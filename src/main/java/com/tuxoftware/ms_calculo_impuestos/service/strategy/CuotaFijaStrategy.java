package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.tuxoftware.ms_calculo_impuestos.dto.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CuotaFijaStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        // 1. Leer el JSON: { "valor": 1.5, "unidad": "UMA" }
        JsonNode json = tarifa.getParametrosRegla();
        BigDecimal valorUnitario = new BigDecimal(json.get("valor").asText());
        String unidad = json.get("unidad").asText();

        BigDecimal total;
        String detalle;

        // 2. Calcular según moneda
        if ("UMA".equalsIgnoreCase(unidad)) {
            BigDecimal costoEnPesos = valorUnitario.multiply(valorUma);
            total = costoEnPesos.multiply(new BigDecimal(solicitud.getCantidad()));
            detalle = String.format("%s UMA (x $%s) x %d ítems", valorUnitario, valorUma, solicitud.getCantidad());
        } else {
            // Son PESOS directos
            total = valorUnitario.multiply(new BigDecimal(solicitud.getCantidad()));
            detalle = String.format("$%s x %d ítems", valorUnitario, solicitud.getCantidad());
        }

        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .subtotal(total)
                .total(total)
                .metodoCalculo("CUOTA_FIJA")
                .detalles(detalle)
                .build();
    }

    @Override
    public String getTipoFormula() {
        return "CUOTA_FIJA"; // Este String debe coincidir con lo que insertes en BD
    }
}
