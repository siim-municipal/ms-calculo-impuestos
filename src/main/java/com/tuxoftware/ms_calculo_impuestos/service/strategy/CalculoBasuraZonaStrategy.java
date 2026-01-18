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

@Component
public class CalculoBasuraZonaStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        JsonNode config = tarifa.getParametrosRegla();
        String zonaSolicitada = "DEFAULT";

        if (solicitud.getParametrosExtra() != null && solicitud.getParametrosExtra().containsKey("zona_clave")) {
            zonaSolicitada = solicitud.getParametrosExtra().get("zona_clave");
        }

        BigDecimal cuotaUma = new BigDecimal(config.path("zonas").path(zonaSolicitada).asText("0"));

        if (cuotaUma.compareTo(BigDecimal.ZERO) == 0) {
            cuotaUma = new BigDecimal(config.path("default").asText("0"));
            zonaSolicitada += " (General)";
        }

        BigDecimal total = cuotaUma.multiply(valorUma).setScale(2, RoundingMode.HALF_UP);

        RubroCalculo rubro = RubroCalculo.builder()
                .concepto("Recolección de Basura")
                .detalles("Zona: " + zonaSolicitada)
                .monto(total)
                .tipo(TipoRubro.CARGO)
                .build();

        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .desglose(new ArrayList<>(List.of(rubro)))
                .total(total)
                .metodoCalculo("BASURA_POR_ZONA")
                .build();
    }
    @Override public String getTipoFormula() { return "BASURA_POR_ZONA"; }
}
