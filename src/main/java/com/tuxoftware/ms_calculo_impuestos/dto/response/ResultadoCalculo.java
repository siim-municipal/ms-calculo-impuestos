package com.tuxoftware.ms_calculo_impuestos.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ResultadoCalculo {

    private String claveConcepto;
    private String descripcion; // Ej: "Expedición de copias certificadas"

    private BigDecimal subtotal; // Antes de redondeos o descuentos
    private BigDecimal total;    // Monto final a pagar

    private String metodoCalculo; // "UMA", "FIJO", "PORCENTAJE"
    private String detalles;      // Ej: "1.5 UMA x 2 documentos x $108.57"
}
