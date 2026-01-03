package com.tuxoftware.ms_calculo_impuestos.service.impl;

import com.tuxoftware.ms_calculo_impuestos.dto.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.persistence.repository.TarifasRepository;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoService;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import com.tuxoftware.ms_calculo_impuestos.service.strategy.CalculoStrategyFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class CalculoServiceImpl implements CalculoService {

    private final TarifasRepository tarifaRepository;
    private final CalculoStrategyFactory strategyFactory;

    // Valor UMA 2024 (Idealmente traer de BD)
    private static final BigDecimal VALOR_UMA_2025 = new BigDecimal("108.57");

    public CalculoServiceImpl(TarifasRepository tarifaRepository, CalculoStrategyFactory strategyFactory) {
        this.tarifaRepository = tarifaRepository;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public ResultadoCalculo calcularImpuesto(SolicitudCalculo solicitud, UUID municipioId) {
        // 1. Determinar año
        int anio = (solicitud.getAnioFiscal() != null) ? solicitud.getAnioFiscal() : LocalDate.now().getYear();

        // 2. Buscar Tarifa (Recuerda que ahora trae JSONB)
        Tarifa tarifa = tarifaRepository.findByClaveConceptoAndMunicipioIdAndAnioFiscal(
                solicitud.getClaveConcepto(),
                municipioId,
                anio
        ).orElseThrow(() -> new RuntimeException("Tarifa no encontrada: " + solicitud.getClaveConcepto()));

        // 3. Obtener la estrategia correcta (Aquí eliminamos el switch gigante)
        // La entidad Tarifa ahora usa getTipoFormula() (String) en vez de getTipoCalculo() (Enum)
        CalculoStrategy estrategia = strategyFactory.getEstrategia(tarifa.getTipoFormula());

        // 4. Ejecutar cálculo
        return estrategia.calcular(solicitud, tarifa, VALOR_UMA_2025);
    }
}
