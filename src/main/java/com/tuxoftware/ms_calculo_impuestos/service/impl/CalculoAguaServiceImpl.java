package com.tuxoftware.ms_calculo_impuestos.service.impl;

import com.tuxoftware.ms_calculo_impuestos.client.AguaClient;
import com.tuxoftware.ms_calculo_impuestos.dto.feign.AguaConsumoResponse;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Uma;
import com.tuxoftware.ms_calculo_impuestos.persistence.repository.TarifasRepository;
import com.tuxoftware.ms_calculo_impuestos.persistence.repository.UmaRepository;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoAguaService;
import com.tuxoftware.ms_calculo_impuestos.service.MunicipioService;
import com.tuxoftware.ms_calculo_impuestos.service.strategy.CalculoStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class CalculoAguaServiceImpl implements CalculoAguaService {

    private final AguaClient aguaClient;
    private final CalculoStrategyFactory strategyFactory;
    private final MunicipioService municipioService;

    private final TarifasRepository tarifaRepository;
    private final UmaRepository umaRepository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularCobroAgua(String contratoIdStr, int mes, int anio, String municipioAlias) {
        UUID contratoId = UUID.fromString(contratoIdStr);

        // 1. Obtener consumo desde MS-AGUA
        AguaConsumoResponse consumoData = aguaClient.obtenerConsumoPeriodo(contratoId, mes, anio);

        // 2. Determinar la CLAVE DE LA TARIFA (Concepto) según el tipo de toma
        // Ej: DOMESTICA -> "AGUA_DOMESTICO"
        String claveConcepto = mapearTipoTomaAConcepto(consumoData.tipoToma());

        // 3. Construir SolicitudCalculo (Corrigiendo Incongruencia #1)
        SolicitudCalculo solicitud = new SolicitudCalculo();
        solicitud.setClaveConcepto(claveConcepto);
        solicitud.setBaseCalculo(consumoData.consumoM3()); // Aquí van los m3
        solicitud.setCantidad(1); // Por defecto 1 servicio
        solicitud.setReferenciaId(contratoIdStr);
        solicitud.setAnioFiscal(anio);
        // Opcional: Pasar metadatos extra si tu estrategia los usa
        solicitud.setParametrosExtra(Map.of(
                "periodo", consumoData.periodo(),
                "esEstimado", String.valueOf(consumoData.estimado())
        ));

        // 4. Obtener Datos Maestros
        UUID municipioId = municipioService.getUuidFromAlias(municipioAlias);

        // A) Obtener la Entidad Tarifa desde BD
        Tarifa tarifa = tarifaRepository.findByClaveConceptoAndMunicipioIdAndAnioFiscal(claveConcepto, municipioId, anio)
                .orElseThrow(() -> new IllegalArgumentException("No existe tarifa para " + claveConcepto + " en " + anio));

        // B) Obtener valor UMA del año
        BigDecimal valorUma = obtenerUmaVigente(anio);

        // 5. Obtener Estrategia y Calcular (Corrigiendo Incongruencia #2)
        // Siempre usamos "AGUA_RANGOS" porque la lógica es la misma, solo cambian los montos de la Tarifa
        var strategy = strategyFactory.getEstrategia("AGUA_RANGOS");

        // Ejecución con la firma correcta
        ResultadoCalculo resultado = strategy.calcular(solicitud, tarifa, valorUma);

        return resultado.getTotal();
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

    private String mapearTipoTomaAConcepto(String tipoTomaAgua) {
        // Mapeo entre el Enum de Agua y las Claves de tu catálogo de Tarifas
        return switch (tipoTomaAgua) {
            case "DOMESTICA" -> "AGUA_DOMESTICO";
            case "COMERCIAL" -> "AGUA_COMERCIAL";
            case "INDUSTRIAL" -> "AGUA_INDUSTRIAL";
            case "MIXTA" -> "AGUA_MIXTO";
            default -> throw new IllegalArgumentException("Tipo de toma desconocido: " + tipoTomaAgua);
        };
    }
}
