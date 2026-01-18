package com.tuxoftware.ms_calculo_impuestos.dto.response;

import com.tuxoftware.ms_calculo_impuestos.enums.TipoRubro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubroCalculo {
    private String concepto;    // Ej: "Impuesto Predial 2026"
    private BigDecimal monto;   // Ej: 1250.00
    private TipoRubro tipo;     // CARGO

    @Builder.Default
    private boolean esImpuestoAdicional = false; // Flag para pintar diferente en UI si quieres

    @Builder.Default
    private String detalles = ""; // Ej: "Tasa 0.005"
}