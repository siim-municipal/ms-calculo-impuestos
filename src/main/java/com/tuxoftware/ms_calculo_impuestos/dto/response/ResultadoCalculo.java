package com.tuxoftware.ms_calculo_impuestos.dto.response;

import com.tuxoftware.ms_calculo_impuestos.enums.TipoRubro;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ResultadoCalculo {

    private String claveConcepto;
    private String descripcion;

    @Builder.Default
    private List<RubroCalculo> desglose = new ArrayList<>();

    private Map<String, Object> metadatos;

    private BigDecimal total;

    private String metodoCalculo;

    /**
     * method utilitario para recalcular el total sumando/restando la lista.
     * Útil para asegurar consistencia antes de enviar la respuesta.
     */
    public void recalcularTotal() {
        this.total = desglose.stream()
                .map(rubro -> {
                    if (rubro.getTipo() == TipoRubro.DESCUENTO) {
                        return rubro.getMonto().negate();
                    } else if (rubro.getTipo() == TipoRubro.INFORMATIVO) {
                        return BigDecimal.ZERO;
                    }
                    return rubro.getMonto();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
