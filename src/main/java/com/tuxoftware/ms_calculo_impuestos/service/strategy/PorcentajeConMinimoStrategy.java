package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.response.RubroCalculo;
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
public class PorcentajeConMinimoStrategy implements CalculoStrategy {
    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        JsonNode json = tarifa.getParametrosRegla();
        BigDecimal tasa = new BigDecimal(json.get("tasa").asText());
        BigDecimal minimoUma = new BigDecimal(json.get("minimo_uma").asText());

        if (solicitud.getBaseCalculo() == null) throw new IllegalArgumentException("Requiere Base Calculo");
        BigDecimal base = solicitud.getBaseCalculo();

        List<RubroCalculo> desglose = new ArrayList<>();

        // 1. Rubro Informativo: Base Gravable
        desglose.add(RubroCalculo.builder()
                .concepto("Valor Catastral (Base)")
                .monto(base)
                .tipo(TipoRubro.INFORMATIVO) // No suma
                .build());

        // 2. Cálculo Matemático Puro
        BigDecimal impuestoCalculado = base.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuestoMinimo = minimoUma.multiply(valorUma).setScale(2, RoundingMode.HALF_UP);

        // 3. Lógica de Desglose
        if (impuestoCalculado.compareTo(impuestoMinimo) >= 0) {
            // Caso Normal: El impuesto supera el mínimo
            desglose.add(RubroCalculo.builder()
                    .concepto(tarifa.getDescripcion())
                    .detalles("Tasa aplicada: " + tasa)
                    .monto(impuestoCalculado)
                    .tipo(TipoRubro.CARGO)
                    .build());
        } else {
            // Caso Mínimo: El impuesto es muy bajo, se ajusta al mínimo
            desglose.add(RubroCalculo.builder()
                    .concepto(tarifa.getDescripcion() + " (Calculado)")
                    .detalles("Tasa aplicada: " + tasa)
                    .monto(impuestoCalculado)
                    .tipo(TipoRubro.CARGO)
                    .build());

            BigDecimal diferencia = impuestoMinimo.subtract(impuestoCalculado);

            desglose.add(RubroCalculo.builder()
                    .concepto("Ajuste a Cuota Mínima")
                    .detalles("El impuesto calculado es menor a " + minimoUma + " UMA")
                    .monto(diferencia)
                    .tipo(TipoRubro.CARGO)
                    .build());
        }

        // Crear objeto y recalcular total (suma de cargos)
        ResultadoCalculo resultado = ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .desglose(desglose)
                .metodoCalculo("PORCENTAJE_CON_MINIMO")
                .metadatos(Map.of(
                        "uma_utilizada", valorUma,
                        "tasa_aplicada", tasa,
                        "minimo_uma_configurado", minimoUma,
                        "base_gravable", base
                ))
                .build();

        resultado.recalcularTotal(); // Suma los rubros CARGO
        return resultado;
    }
    @Override public String getTipoFormula() { return "PORCENTAJE_CON_MINIMO"; }
}
