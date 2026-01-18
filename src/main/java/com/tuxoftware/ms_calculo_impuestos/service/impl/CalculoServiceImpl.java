package com.tuxoftware.ms_calculo_impuestos.service.impl;

import com.tuxoftware.ms_calculo_impuestos.client.PadronClient;
import com.tuxoftware.ms_calculo_impuestos.dto.response.InfoFiscalDTO;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.response.RubroCalculo;
import com.tuxoftware.ms_calculo_impuestos.enums.TipoRubro;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Uma;
import com.tuxoftware.ms_calculo_impuestos.persistence.repository.TarifasRepository;
import com.tuxoftware.ms_calculo_impuestos.persistence.repository.UmaRepository;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoService;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import com.tuxoftware.ms_calculo_impuestos.service.MunicipioService;
import com.tuxoftware.ms_calculo_impuestos.service.strategy.CalculoStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculoServiceImpl implements CalculoService {

    private final TarifasRepository tarifaRepository;
    private final CalculoStrategyFactory strategyFactory;
    private final MunicipioService municipioService;
    private final UmaRepository umaRepository;
    private final PadronClient padronClient;

    @Value("${app.calculo.impuesto-adicional:0.10}")
    private BigDecimal porcentajeAdicional;

    @Override
    @Transactional(readOnly = true)
    public ResultadoCalculo calcularImpuesto(SolicitudCalculo solicitud, String municipioAlias) {
        // 1. Configuración Inicial
        int anio = (solicitud.getAnioFiscal() != null) ? solicitud.getAnioFiscal() : LocalDate.now().getYear();
        UUID municipioId = municipioService.getUuidFromAlias(municipioAlias);

        // 2. Obtener UMA (con Fallback)
        BigDecimal valorUma = obtenerUmaVigente(anio);

        // 3. Obtener Tarifa
        Tarifa tarifa = tarifaRepository.findByClaveConceptoAndMunicipioIdAndAnioFiscal(
                solicitud.getClaveConcepto(), municipioId, anio
        ).orElseThrow(() -> new RuntimeException("Tarifa no encontrada: " + solicitud.getClaveConcepto()));

        // 4. Resolver Base Gravable (Orquestación Padrón vs Manual)
        resolverBaseGravable(solicitud, tarifa, municipioAlias);

        // 5. Ejecutar Estrategia Matemática
        CalculoStrategy estrategia = strategyFactory.getEstrategia(tarifa.getTipoFormula());
        ResultadoCalculo resultado = estrategia.calcular(solicitud, tarifa, valorUma);

        // 6. Aplicar Impuesto Adicional (Art. 43)
        aplicarImpuestoAdicional(resultado, tarifa);

        // 7. Recalcular Total Final (Suma de toda la lista)
        resultado.recalcularTotal();

        return resultado;
    }

    private BigDecimal obtenerUmaVigente(int anio) {
        return umaRepository.findByAnio(anio)
                .map(Uma::getValorDiario)
                .orElseGet(() -> {
                    log.warn("UMA del año {} no encontrada. Usando la más reciente.", anio);
                    // Asumimos un method en Repo o lógica para traer la última
                    return umaRepository.findTopByOrderByAnioDesc()
                            .map(Uma::getValorDiario)
                            .orElseThrow(() -> new RuntimeException("Error Crítico: No hay configuración de UMA en el sistema."));
                });
    }

    private void resolverBaseGravable(SolicitudCalculo solicitud, Tarifa tarifa, String tenantId) {
        if (solicitud.getReferenciaId() != null && !solicitud.getReferenciaId().isBlank()) {
            try {
                // Delegamos a la consulta segura
                BigDecimal baseRemota = consultarPadronSeguro(solicitud.getReferenciaId(), tarifa, tenantId);
                solicitud.setBaseCalculo(baseRemota);
                log.info("Base gravable validada y obtenida: {}", baseRemota);
            } catch (SecurityException se) {
                // Re-lanzamos errores de seguridad para que suban como 403 Forbidden
                log.error("ALERTA DE SEGURIDAD: {}", se.getMessage());
                throw se;
            } catch (Exception e) {
                if (solicitud.getBaseCalculo() == null) throw e;
                log.warn("Fallo al consultar Padrón, usando base manual. Error: {}", e.getMessage());
            }
        }
    }

    private BigDecimal consultarPadronSeguro(String referenciaId, Tarifa tarifa, String tokenAlias) {
        String tipo = tarifa.getParametrosRegla().path("tipo_objeto").asText("GENERICO");
        InfoFiscalDTO infoRemota;

        // Obtener DTO completo según el tipo
        switch (tipo) {
            case "PREDIO" -> infoRemota = padronClient.obtenerInfoPredio(referenciaId);
            case "LICENCIA" -> infoRemota = padronClient.obtenerInfoLicencia(referenciaId);
            default -> throw new IllegalArgumentException("Tipo no soportado para consulta remota: " + tipo);
        }

        // 🛡CHECKPOINT DE SEGURIDAD (Tenant Isolation)
        if (!infoRemota.municipioAlias().equals(tokenAlias)) {
            throw new SecurityException("Tenant Mismatch");
        }

        // Validación de Negocio (Opcional)
        if (!"ACTIVO".equals(infoRemota.estatus())) {
            throw new IllegalArgumentException("El recurso solicitado no está activo (Estatus: " + infoRemota.estatus() + ")");
        }

        return infoRemota.valorBase();
    }

    private void aplicarImpuestoAdicional(ResultadoCalculo resultado, Tarifa tarifa) {
        if (Boolean.TRUE.equals(tarifa.getAplicaAdicional())) {

            // Calculamos la base sobre la cual aplica el adicional (suma de cargos actuales)
            BigDecimal sumaCargos = resultado.getDesglose().stream()
                    .filter(r -> r.getTipo() == TipoRubro.CARGO)
                    .map(RubroCalculo::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal montoAdicional = sumaCargos.multiply(porcentajeAdicional)
                    .setScale(2, RoundingMode.HALF_UP);

            if (montoAdicional.compareTo(BigDecimal.ZERO) > 0) {
                resultado.getDesglose().add(RubroCalculo.builder()
                        .concepto("Impuesto Adicional (Ecológico/Infraestructura)")
                        .monto(montoAdicional)
                        .tipo(TipoRubro.CARGO)
                        .esImpuestoAdicional(true) // Flag para pintar ROJO en Front
                        .detalles(porcentajeAdicional.multiply(BigDecimal.valueOf(100)) + "%")
                        .build());
            }
        }
    }
}
