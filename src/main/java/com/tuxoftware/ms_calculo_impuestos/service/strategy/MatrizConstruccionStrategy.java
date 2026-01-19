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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MatrizConstruccionStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        BigDecimal baseGravable = solicitud.getBaseCalculo(); // M2 o ML
        JsonNode config = tarifa.getParametrosRegla();
        String selectorKey = config.path("selector_key").asText("subtipo");
        String subtipo = solicitud.getParametrosExtra().get(selectorKey);
        BigDecimal factorAplicable = BigDecimal.ZERO;

        JsonNode reglaSubtipo = config.path("reglas").path(subtipo);
        String modoCobro = reglaSubtipo.path("modo_cobro").asText();

        List<RubroCalculo> desglose = new ArrayList<>();

        // Informativo
        desglose.add(RubroCalculo.builder()
                .concepto("Superficie/Longitud Declarada")
                .monto(baseGravable)
                .detalles(modoCobro.equals("ESCALONADO_EXCEDENTE") ? "ML" : "M2")
                .tipo(TipoRubro.INFORMATIVO)
                .build());

        BigDecimal totalUma;
        String detalleCalculo = "";

        if ("FACTOR_POR_UNIDAD".equals(modoCobro)) {
            boolean rangoEncontrado = false;

            for (JsonNode rango : reglaSubtipo.path("rangos")) {
                BigDecimal min = new BigDecimal(rango.get("min").asText());
                BigDecimal max = new BigDecimal(rango.get("max").asText());

                if (baseGravable.compareTo(min) >= 0 && baseGravable.compareTo(max) <= 0) {
                    factorAplicable = new BigDecimal(rango.get("valor").asText());
                    rangoEncontrado = true;
                    break;
                }
            }

            if (!rangoEncontrado) {
                throw new RuntimeException("Los M2 proporcionados no caen en ningún rango configurado.");
            }

            // Fórmula: M2 * FactorUMA
            totalUma = baseGravable.multiply(factorAplicable);
            detalleCalculo = String.format(
                    "Subtipo: %s. Rango detectado. Factor: %s UMA/m2 Total del UMA: %s",
                    subtipo, factorAplicable, totalUma);

            BigDecimal total = baseGravable.multiply(factorAplicable).multiply(valorUma).setScale(2, RoundingMode.HALF_UP);

            desglose.add(RubroCalculo.builder()
                    .concepto("Licencia de Construcción")
                    .detalles(detalleCalculo)
                    .monto(total)
                    .tipo(TipoRubro.CARGO)
                    .build());

        } else if ("ESCALONADO_EXCEDENTE".equals(modoCobro)) {
            // Lógica de Bardas: Primeros X metros precio A, siguientes precio B
            BigDecimal limiteBase = new BigDecimal(reglaSubtipo.path("limite_base").asText());
            BigDecimal costoBase = new BigDecimal(reglaSubtipo.path("costo_base").asText()).multiply(valorUma);
            BigDecimal costoExcedente = new BigDecimal(reglaSubtipo.path("costo_excedente").asText()).multiply(valorUma);

            // Parte Base
            BigDecimal metrosBase = baseGravable.min(limiteBase);
            desglose.add(RubroCalculo.builder()
                    .concepto("Derechos (Tarifa Base)")
                    .detalles(String.format("Primeros %s ML", metrosBase))
                    .monto(metrosBase.multiply(costoBase).setScale(2, RoundingMode.HALF_UP))
                    .tipo(TipoRubro.CARGO)
                    .build());

            // Parte Excedente
            if (baseGravable.compareTo(limiteBase) > 0) {
                BigDecimal metrosExtra = baseGravable.subtract(limiteBase);
                desglose.add(RubroCalculo.builder()
                        .concepto("Derechos (Excedente)")
                        .detalles(String.format("%s ML adicionales", metrosExtra))
                        .monto(metrosExtra.multiply(costoExcedente).setScale(2, RoundingMode.HALF_UP))
                        .tipo(TipoRubro.CARGO)
                        .build());
            }
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("uma_utilizada", valorUma);
        metadata.put("subtipo_obra", subtipo);
        metadata.put("modo_cobro", modoCobro);

        if ("FACTOR_POR_UNIDAD".equals(modoCobro)) {
            metadata.put("factor_m2_aplicado", factorAplicable);
        }

        ResultadoCalculo res = ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .desglose(desglose)
                .metodoCalculo("MATRIZ_CONSTRUCCION")
                .metadatos(metadata)
                .build();

        res.recalcularTotal();
        return res;
    }
    @Override public String getTipoFormula() { return "MATRIZ_CONSTRUCCION"; }
}
