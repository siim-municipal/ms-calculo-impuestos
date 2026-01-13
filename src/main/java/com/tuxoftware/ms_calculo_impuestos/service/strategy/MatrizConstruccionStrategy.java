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
public class MatrizConstruccionStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        // 1. Obtener la base gravable (Metros Cuadrados o Lineales)
        BigDecimal baseGravable = solicitud.getBaseCalculo();
        if (baseGravable == null) {
            throw new IllegalArgumentException("Se requieren los M2 o ML en 'baseCalculo' para Licencia de Construcción");
        }

        // 2. Leer configuración y selector
        JsonNode config = tarifa.getParametrosRegla();
        String selectorKey = config.path("selector_key").asText("subtipo");

        // 3. Determinar el subtipo solicitado (HABITACIONAL, COMERCIAL, BARDAS...)
        if (solicitud.getParametrosExtra() == null || !solicitud.getParametrosExtra().containsKey(selectorKey)) {
            throw new IllegalArgumentException("Falta el parámetro extra: " + selectorKey);
        }
        String subtipo = solicitud.getParametrosExtra().get(selectorKey);

        // 4. Buscar la regla específica para ese subtipo
        JsonNode reglaSubtipo = config.path("reglas").path(subtipo);
        if (reglaSubtipo.isMissingNode()) {
            throw new IllegalArgumentException("El subtipo '" + subtipo + "' no está configurado en las tarifas de construcción.");
        }

        String modoCobro = reglaSubtipo.path("modo_cobro").asText();
        BigDecimal totalUma = BigDecimal.ZERO;
        String detalleCalculo = "";

        // 5. Aplicar lógica según el modo de cobro interno
        if ("FACTOR_POR_UNIDAD".equals(modoCobro)) {
            // Caso Habitacional/Comercial: Busca en qué rango caen los M2 y multiplica M2 * Factor
            BigDecimal factorAplicable = BigDecimal.ZERO;
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
            detalleCalculo = String.format("Subtipo: %s. Rango detectado. Factor: %s UMA/m2", subtipo, factorAplicable);

        } else if ("ESCALONADO_EXCEDENTE".equals(modoCobro)) {
            // Caso Bardas: Primeros 50m a precio X, el resto a precio Y
            BigDecimal limiteBase = new BigDecimal(reglaSubtipo.path("limite_base").asText());
            BigDecimal costoBase = new BigDecimal(reglaSubtipo.path("costo_base").asText());
            BigDecimal costoExcedente = new BigDecimal(reglaSubtipo.path("costo_excedente").asText());

            if (baseGravable.compareTo(limiteBase) <= 0) {
                // Solo cobra la base (por metro)
                totalUma = baseGravable.multiply(costoBase);
            } else {
                // Cobra los primeros X metros a precio base
                BigDecimal parteBase = limiteBase.multiply(costoBase);
                // Cobra el resto a precio excedente
                BigDecimal excedente = baseGravable.subtract(limiteBase);
                BigDecimal parteExcedente = excedente.multiply(costoExcedente);

                totalUma = parteBase.add(parteExcedente);
            }
            detalleCalculo = String.format("Subtipo: %s. Esquema escalonado con límite base de %s m.", subtipo, limiteBase);
        } else {
            throw new UnsupportedOperationException("Modo de cobro no soportado: " + modoCobro);
        }

        // 6. Convertir UMA a Pesos
        BigDecimal totalPesos = totalUma.multiply(valorUma);

        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion() + " - " + reglaSubtipo.path("descripcion").asText())
                .subtotal(totalPesos.setScale(2, RoundingMode.HALF_UP))
                .total(totalPesos.setScale(2, RoundingMode.HALF_UP))
                .metodoCalculo("MATRIZ_CONSTRUCCION")
                .detalles(detalleCalculo)
                .build();
    }

    @Override
    public String getTipoFormula() { return "MATRIZ_CONSTRUCCION"; }
}
