package com.tuxoftware.ms_calculo_impuestos.service.impl;

import com.tuxoftware.ms_calculo_impuestos.client.PadronClient;
import com.tuxoftware.ms_calculo_impuestos.dto.feign.LicenciaDetalleDTO;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
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
        resolverBaseGravable(solicitud, tarifa);

        // 5. Ejecutar Estrategia Matemática
        CalculoStrategy estrategia = strategyFactory.getEstrategia(tarifa.getTipoFormula());
        ResultadoCalculo resultado = estrategia.calcular(solicitud, tarifa, valorUma);

        // 6. Aplicar Impuesto Adicional (Art. 43)
        aplicarImpuestoAdicional(resultado, tarifa);

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

    private void resolverBaseGravable(SolicitudCalculo solicitud, Tarifa tarifa) {
        if (solicitud.getReferenciaId() != null && !solicitud.getReferenciaId().isBlank()) {
            try {
                BigDecimal baseRemota = consultarPadron(solicitud.getReferenciaId(), tarifa);
                solicitud.setBaseCalculo(baseRemota);
                log.info("Base gravable obtenida de Padrón: {}", baseRemota);
            } catch (Exception e) {
                if (solicitud.getBaseCalculo() == null) throw e;
                log.warn("Fallo al consultar Padrón, usando base manual proporcionada.");
            }
        }
    }

    private BigDecimal consultarPadron(String referenciaId, Tarifa tarifa) {
        // Usamos un campo de la tarifa para saber a qué endpoint llamar
        // Asumimos que Tarifa tiene un campo 'tipoObjeto' o deducimos por prefijo
        String tipo = tarifa.getParametrosRegla().path("tipo_objeto").asText("GENERICO");

        return switch (tipo) {
            case "PREDIO" -> padronClient.obtenerValorCatastral(referenciaId);
            case "LICENCIA" -> {
                LicenciaDetalleDTO licencia = padronClient.obtenerDetalleLicencia(referenciaId);
                yield licencia.metrosCuadrados(); // O la propiedad que sirva de base
            }
            default -> throw new IllegalArgumentException("No se puede resolver referencia automática para tipo: " + tipo);
        };
    }

    private void aplicarImpuestoAdicional(ResultadoCalculo resultado, Tarifa tarifa) {
        if (Boolean.TRUE.equals(tarifa.getAplicaAdicional())) {
            BigDecimal subtotal = resultado.getTotal();
            BigDecimal adicional = subtotal.multiply(porcentajeAdicional);
            BigDecimal nuevoTotal = subtotal.add(adicional).setScale(2, RoundingMode.HALF_UP);
            resultado.setTotal(nuevoTotal);

            String porcentajeTexto = porcentajeAdicional.multiply(new BigDecimal(100))
                    .stripTrailingZeros().toPlainString();

            resultado.setDetalles(resultado.getDetalles() +
                    String.format(" [+%s%% Adicional Ecológico: $%s]",
                            porcentajeTexto,
                            adicional.setScale(2, RoundingMode.HALF_UP)));
        }
    }
}
