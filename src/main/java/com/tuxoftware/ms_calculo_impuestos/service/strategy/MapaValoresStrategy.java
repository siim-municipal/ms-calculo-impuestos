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
public class MapaValoresStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        String giro = solicitud.getParametrosExtra().get("giro_clave");
        String tipoTramite = solicitud.getParametrosExtra().getOrDefault("tipo_tramite", "revalidacion");

        JsonNode json = tarifa.getParametrosRegla();
        JsonNode nodoGiro = json.path("giros").path(giro);

        if (nodoGiro.isMissingNode()) {
            throw new RuntimeException("Giro no configurado: " + giro);
        }

        BigDecimal cuotaUma = new BigDecimal(nodoGiro.path(tipoTramite).asText());
        BigDecimal total = cuotaUma.multiply(valorUma).setScale(2, RoundingMode.HALF_UP);

        RubroCalculo rubro = RubroCalculo.builder()
                .concepto(tarifa.getDescripcion()) // Ej: "Licencia de Funcionamiento"
                .detalles(String.format("Giro: %s (%s)", giro, tipoTramite))
                .monto(total)
                .tipo(TipoRubro.CARGO)
                .build();

        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .desglose(new ArrayList<>(List.of(rubro)))
                .total(total)
                .metodoCalculo("MAPA_VALORES")
                .build();
    }
    @Override public String getTipoFormula() { return "MAPA_VALORES"; }
}