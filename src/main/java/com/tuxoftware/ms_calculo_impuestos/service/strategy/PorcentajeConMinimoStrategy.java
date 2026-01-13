package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PorcentajeConMinimoStrategy implements CalculoStrategy {
    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        JsonNode json = tarifa.getParametrosRegla();
        BigDecimal tasa = new BigDecimal(json.get("tasa").asText());
        BigDecimal minimoUma = new BigDecimal(json.get("minimo_uma").asText());

        if (solicitud.getBaseCalculo() == null) {
            throw new IllegalArgumentException("Este concepto requiere una base de cálculo (Valor Catastral).");
        }
        BigDecimal base = solicitud.getBaseCalculo(); // Valor Catastral

        BigDecimal impuestoCalculado = base.multiply(tasa);
        BigDecimal impuestoMinimo = minimoUma.multiply(valorUma);

        BigDecimal total = impuestoCalculado.max(impuestoMinimo); // Toma el mayor

        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .subtotal(impuestoCalculado.setScale(2, RoundingMode.HALF_UP))
                .total(total.setScale(2, RoundingMode.HALF_UP))
                .metodoCalculo("PORCENTAJE_CON_MINIMO")
                .detalles(String.format("Tasa: %s. Minimo UMA: %s", tasa, minimoUma))
                .build();
    }
    @Override
    public String getTipoFormula() { return "PORCENTAJE_CON_MINIMO"; }
}
