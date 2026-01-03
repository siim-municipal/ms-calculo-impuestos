package com.tuxoftware.ms_calculo_impuestos.dto.rules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rango {

    // Límite inferior (Ej. 0)
    private BigDecimal min;

    // Límite superior (Ej. 50). Usar un número muy grande para "En adelante"
    private BigDecimal max;

    // El costo por unidad dentro de este rango (Ej. 0.30 UMA)
    private BigDecimal costoUnitario;

    // Opcional: Algunos impuestos tienen una cuota fija base al entrar al rango
    private BigDecimal cuotaFijaBase;
}