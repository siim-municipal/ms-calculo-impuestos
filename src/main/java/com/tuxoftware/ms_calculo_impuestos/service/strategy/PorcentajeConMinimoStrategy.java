package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.tuxoftware.ms_calculo_impuestos.dto.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PorcentajeConMinimoStrategy implements CalculoStrategy {
    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        JsonNode json = tarifa.getParametrosRegla();
        BigDecimal tasa = new BigDecimal(json.get("tasa").asText());
        BigDecimal minimoUma = new BigDecimal(json.get("minimo_uma").asText());

        BigDecimal base = solicitud.getBaseCalculo(); // Valor Catastral

        BigDecimal impuestoCalculado = base.multiply(tasa);
        BigDecimal impuestoMinimo = minimoUma.multiply(valorUma);

        BigDecimal total = impuestoCalculado.max(impuestoMinimo); // Toma el mayor

        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .subtotal(impuestoCalculado)
                .total(total)
                .metodoCalculo("PORCENTAJE_CON_MINIMO")
                .detalles(String.format("Tasa: %s. Minimo UMA: %s", tasa, minimoUma))
                .build();
    }
    @Override
    public String getTipoFormula() { return "PORCENTAJE_CON_MINIMO"; }
}
