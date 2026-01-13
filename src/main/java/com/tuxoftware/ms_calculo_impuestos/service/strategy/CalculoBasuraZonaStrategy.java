package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
        double cuotaUma = config.path("zonas").path(zonaSolicitada).asDouble();

        // Si no encuentra la zona, usa el default
        if (cuotaUma == 0) {
            cuotaUma = config.path("default").asDouble();
            // Opcional: indicar en el detalle que se usó la tarifa default
            zonaSolicitada = zonaSolicitada + " (No encontrada, aplicando tarifa general)";
        }

        BigDecimal total = new BigDecimal(cuotaUma).multiply(valorUma);

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
