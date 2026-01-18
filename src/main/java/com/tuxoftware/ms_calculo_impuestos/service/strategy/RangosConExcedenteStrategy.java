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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class RangosConExcedenteStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        BigDecimal baseGravable = solicitud.getBaseCalculo();
        JsonNode tabulador = tarifa.getParametrosRegla().get("tabulador");

        BigDecimal cuotaFija = BigDecimal.ZERO;
        BigDecimal tasaExcedente = BigDecimal.ZERO;
        BigDecimal limiteInferior = BigDecimal.ZERO;

        // Búsqueda del Rango
        Iterator<JsonNode> elements = tabulador.elements();
        while (elements.hasNext()) {
            JsonNode rango = elements.next();
            BigDecimal limInf = new BigDecimal(rango.get("lim_inf").asText());
            BigDecimal limSup = new BigDecimal(rango.get("lim_sup").asText());

            if (baseGravable.compareTo(limInf) >= 0 && baseGravable.compareTo(limSup) <= 0) {
                limiteInferior = limInf;
                cuotaFija = new BigDecimal(rango.get("cuota_fija").asText());
                tasaExcedente = new BigDecimal(rango.get("tasa_excedente").asText());
                break;
            }
        }

        List<RubroCalculo> desglose = new ArrayList<>();

        // 1. Informativo
        desglose.add(RubroCalculo.builder()
                .concepto("Base Gravable (Operación)")
                .monto(baseGravable)
                .tipo(TipoRubro.INFORMATIVO)
                .build());

        // 2. Cuota Fija del Rango
        if (cuotaFija.compareTo(BigDecimal.ZERO) > 0) {
            desglose.add(RubroCalculo.builder()
                    .concepto("Cuota Fija (Por Rango)")
                    .monto(cuotaFija.setScale(2, RoundingMode.HALF_UP))
                    .tipo(TipoRubro.CARGO)
                    .build());
        }

        // 3. Impuesto Marginal (Excedente)
        BigDecimal excedente = baseGravable.subtract(limiteInferior);
        BigDecimal impuestoMarginal = excedente.multiply(tasaExcedente).setScale(2, RoundingMode.HALF_UP);

        if (impuestoMarginal.compareTo(BigDecimal.ZERO) > 0) {
            String tasaPct = tasaExcedente.multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString();
            desglose.add(RubroCalculo.builder()
                    .concepto("Impuesto s/Excedente")
                    .detalles(String.format("(Base - %s) x %s%%", limiteInferior, tasaPct))
                    .monto(impuestoMarginal)
                    .tipo(TipoRubro.CARGO)
                    .build());
        }

        ResultadoCalculo res = ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .desglose(desglose)
                .metodoCalculo("RANGOS_CON_EXCEDENTE")
                .metadatos(Map.of(
                        "limite_inferior_rango", limiteInferior,
                        "tasa_excedente_aplicada", tasaExcedente,
                        "cuota_fija_rango", cuotaFija,
                        "base_operacion", solicitud.getBaseCalculo()
                ))
                .build();

        res.recalcularTotal();
        return res;
    }
    @Override public String getTipoFormula() { return "RANGOS_CON_EXCEDENTE"; }
}
