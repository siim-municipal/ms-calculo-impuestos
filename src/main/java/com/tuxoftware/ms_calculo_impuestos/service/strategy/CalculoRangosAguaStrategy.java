package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.response.RubroCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.rules.ConfigRangos;
import com.tuxoftware.ms_calculo_impuestos.dto.rules.Rango;
import com.tuxoftware.ms_calculo_impuestos.enums.TipoRubro;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CalculoRangosAguaStrategy implements CalculoStrategy {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        ConfigRangos config = mapper.convertValue(tarifa.getParametrosRegla(), ConfigRangos.class);
        BigDecimal consumo = solicitud.getBaseCalculo(); // m3

        if (consumo == null) throw new IllegalArgumentException("Requiere m3 en baseCalculo");

        BigDecimal factorUma = null;
        // Buscar rango
        for (Rango r : config.getRangos()) {
            if (consumo.compareTo(r.getMin()) >= 0 && consumo.compareTo(r.getMax()) <= 0) {
                factorUma = r.getCostoUnitario();
                break;
            }
        }

        // (validación null factorUma)
        if (factorUma == null) throw new RuntimeException("Rango no encontrado para consumo: " + consumo);

        List<RubroCalculo> desglose = new ArrayList<>();

        // Rubro Informativo
        desglose.add(RubroCalculo.builder()
                .concepto("Consumo de Agua")
                .monto(consumo)
                .detalles("Metros cúbicos (m3)")
                .tipo(TipoRubro.INFORMATIVO)
                .build());

        // Rubro Cargo
        BigDecimal total = factorUma.multiply(valorUma).multiply(consumo).setScale(2, RoundingMode.HALF_UP);

        desglose.add(RubroCalculo.builder()
                .concepto(tarifa.getDescripcion())
                .detalles(String.format("Rango aplicado: %s UMA/m3", factorUma))
                .monto(total)
                .tipo(TipoRubro.CARGO)
                .build());

        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .desglose(desglose)
                .total(total)
                .metodoCalculo("AGUA_RANGOS")
                .metadatos(Map.of(
                        "uma_utilizada", valorUma,
                        "consumo_registrado", consumo,
                        "factor_rango_aplicado", factorUma // Ej: 1.25 UMA/m3
                ))
                .build();
    }
    @Override public String getTipoFormula() { return "AGUA_RANGOS"; }
}
