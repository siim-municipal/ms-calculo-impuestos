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
public class MapaValoresStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        // Parametros requeridos: "giro" y "tipo_tramite" (expedicion/revalidacion)
        String giro = solicitud.getParametrosExtra().get("giro_clave");
        String tipoTramite = solicitud.getParametrosExtra().getOrDefault("tipo_tramite", "revalidacion");

        JsonNode json = tarifa.getParametrosRegla();
        JsonNode nodoGiro = json.path("giros").path(giro);

        if (nodoGiro.isMissingNode()) {
            throw new RuntimeException("El giro '" + giro + "' no existe en la configuración de la tarifa.");
        }

        BigDecimal cuotaUma = new BigDecimal(nodoGiro.path(tipoTramite).asText());
        BigDecimal total = cuotaUma.multiply(valorUma);

        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .subtotal(total.setScale(2, RoundingMode.HALF_UP))
                .total(total.setScale(2, RoundingMode.HALF_UP))
                .metodoCalculo("MAPA_VALORES")
                .detalles(String.format("Giro: %s. Trámite: %s. Cuota: %s UMA", giro, tipoTramite, cuotaUma))
                .build();
    }

    @Override
    public String getTipoFormula() { return "MAPA_VALORES"; }
}