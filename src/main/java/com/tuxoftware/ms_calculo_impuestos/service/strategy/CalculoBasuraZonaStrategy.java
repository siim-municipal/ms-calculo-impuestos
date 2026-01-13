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
public class CalculoBasuraZonaStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        JsonNode config = tarifa.getParametrosRegla();

        // Validación defensiva: asegurar que vienen parámetros extra
        String zonaSolicitada = "DEFAULT";
        if (solicitud.getParametrosExtra() != null && solicitud.getParametrosExtra().containsKey("zona_clave")) {
            zonaSolicitada = solicitud.getParametrosExtra().get("zona_clave");
        }

        // Buscar el valor en el JSON
        BigDecimal cuotaUma = new BigDecimal(config.path("zonas").path(zonaSolicitada).asText("0"));

        // Si no encuentra la zona, usa el default
        if (cuotaUma.compareTo(BigDecimal.ZERO) == 0) {
            cuotaUma = new BigDecimal(config.path("default").asText("0"));
            zonaSolicitada = zonaSolicitada + " (No encontrada, aplicando tarifa general)";
        }

        BigDecimal total = cuotaUma.multiply(valorUma).setScale(2, RoundingMode.HALF_UP);

        // Construcción completa del resultado
        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .subtotal(total)
                .total(total)
                .metodoCalculo("BASURA_POR_ZONA")
                .detalles(String.format("Zona: %s. Cuota: %s UMA", zonaSolicitada, cuotaUma))
                .build();
    }

    @Override
    public String getTipoFormula() { return "BASURA_POR_ZONA"; }
}
