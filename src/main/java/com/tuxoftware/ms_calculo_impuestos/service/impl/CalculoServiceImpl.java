package com.tuxoftware.ms_calculo_impuestos.service.impl;

import com.tuxoftware.ms_calculo_impuestos.dto.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.persistence.repository.TarifasRepository;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoService;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import com.tuxoftware.ms_calculo_impuestos.service.MunicipioService;
import com.tuxoftware.ms_calculo_impuestos.service.strategy.CalculoStrategyFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class CalculoServiceImpl implements CalculoService {

    private final TarifasRepository tarifaRepository;
    private final CalculoStrategyFactory strategyFactory;
    private final MunicipioService municipioService;

    // Valor UMA 2024
    // TODO: (Idealmente traer de BD)
    private static final BigDecimal VALOR_UMA_2025 = new BigDecimal("108.57");

    public CalculoServiceImpl(TarifasRepository tarifaRepository, CalculoStrategyFactory strategyFactory, MunicipioService municipioService) {
        this.tarifaRepository = tarifaRepository;
        this.strategyFactory = strategyFactory;
        this.municipioService = municipioService;
    }

    @Override
    public ResultadoCalculo calcularImpuesto(SolicitudCalculo solicitud, String municipioAlias) {
        // 1. Determinar año
        int anio = (solicitud.getAnioFiscal() != null) ? solicitud.getAnioFiscal() : LocalDate.now().getYear();

        UUID municipioId = municipioService.getUuidFromAlias(municipioAlias);

        // 2. Buscar Tarifa (ahora trae JSONB)
        Tarifa tarifa = tarifaRepository.findByClaveConceptoAndMunicipioIdAndAnioFiscal(
                solicitud.getClaveConcepto(),
                municipioId,
                anio
        ).orElseThrow(() -> new RuntimeException("Tarifa no encontrada: " + solicitud.getClaveConcepto()));

        // 3. Obtener la estrategia correcta
        CalculoStrategy estrategia = strategyFactory.getEstrategia(tarifa.getTipoFormula());

        // 4. Ejecutar cálculo
        return estrategia.calcular(solicitud, tarifa, VALOR_UMA_2025);
    }
}
