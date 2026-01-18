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
import java.util.Map;

@Component
public class CuotaFijaStrategy implements CalculoStrategy {

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        JsonNode json = tarifa.getParametrosRegla();
        BigDecimal valorUnitario = new BigDecimal(json.get("valor").asText());
        String unidad = json.get("unidad").asText();

        BigDecimal montoTotal;
        String detalleUnitario;

        // 1. Calcular Monto
        if ("UMA".equalsIgnoreCase(unidad)) {
            BigDecimal costoEnPesos = valorUnitario.multiply(valorUma);
            montoTotal = costoEnPesos.multiply(new BigDecimal(solicitud.getCantidad()));
            detalleUnitario = String.format("%s UMA (x $%s)", valorUnitario, valorUma);
        } else {
            montoTotal = valorUnitario.multiply(new BigDecimal(solicitud.getCantidad()));
            detalleUnitario = String.format("$%s Pesos", valorUnitario);
        }

        montoTotal = montoTotal.setScale(2, RoundingMode.HALF_UP);

        // 2. Crear Rubro
        RubroCalculo rubroPrincipal = RubroCalculo.builder()
                .concepto(tarifa.getDescripcion())
                .monto(montoTotal)
                .tipo(TipoRubro.CARGO)
                .detalles(String.format("%s x %d ítems", detalleUnitario, solicitud.getCantidad()))
                .build();

        // 3. Retornar Resultado con Lista
        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion())
                .desglose(new ArrayList<>(List.of(rubroPrincipal))) // Lista mutable
                .metadatos(Map.of("valor_unitario", valorUnitario, "unidad", unidad))
                .total(montoTotal)
                .metodoCalculo("CUOTA_FIJA")
                .metadatos(Map.of(
                        "uma_utilizada", valorUma,
                        "valor_unitario_configurado", valorUnitario,
                        "unidad_medida", unidad,
                        "cantidad_solicitada", solicitud.getCantidad()
                ))
                .build();
    }

    @Override public String getTipoFormula() { return "CUOTA_FIJA"; }
}
