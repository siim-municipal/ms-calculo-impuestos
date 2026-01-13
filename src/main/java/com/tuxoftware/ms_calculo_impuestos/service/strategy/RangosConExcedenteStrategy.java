package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;

@Component
public class RangosConExcedenteStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        BigDecimal baseGravable = solicitud.getBaseCalculo(); // Valor de la operación
        if (baseGravable == null) throw new IllegalArgumentException("Se requiere baseCalculo para Traslado de Dominio");

        JsonNode reglas = tarifa.getParametrosRegla();
        JsonNode tabulador = reglas.get("tabulador");

        BigDecimal cuotaFija = BigDecimal.ZERO;
        BigDecimal tasaExcedente = BigDecimal.ZERO;
        BigDecimal limiteInferior = BigDecimal.ZERO;

        // Buscar el rango: LimiteInf <= Base <= LimiteSup
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

        // Fórmula: CuotaFija + ( (Base - LimInf) * Tasa )
        BigDecimal excedente = baseGravable.subtract(limiteInferior);
        BigDecimal impuestoMarginal = excedente.multiply(tasaExcedente);
        BigDecimal total = cuotaFija.add(impuestoMarginal);

        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .subtotal(total.setScale(2, RoundingMode.HALF_UP))
                .total(total.setScale(2, RoundingMode.HALF_UP))
                .metodoCalculo("RANGOS_CON_EXCEDENTE")
                .detalles(String.format("Base: $%s. Rango aplicado con tasa del %s%% sobre excedente.",
                        baseGravable,
                        tasaExcedente.multiply(new BigDecimal(100))))
                .build();
    }

    @Override
    public String getTipoFormula() { return "RANGOS_CON_EXCEDENTE"; }
}
