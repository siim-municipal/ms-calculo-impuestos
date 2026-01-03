package com.tuxoftware.ms_calculo_impuestos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class SolicitudCalculo {

    @NotBlank(message = "La clave del concepto es obligatoria (Ej. AGUA_DOMESTICO)")
    private String claveConcepto;

    /**
     * Cantidad de ítems discretos (enteros).
     * Úsalo para conteos simples.
     * Ej: 2 copias certificadas, 1 licencia, 3 constancias.
     * Valor por defecto: 1.
     */
    @NotNull
    private Integer cantidad = 1;

    /**
     * BASE DE CÁLCULO (Lo que antes llamamos 'consumo' o 'valorMedida').
     * Es la medida continua sobre la que se aplican rangos o porcentajes.
     * * Ejemplos de uso según el trámite:
     * - Agua Potable: Aquí envías los m3 consumidos (Ej. 65.50).
     * - Construcción: Aquí envías los m2 de superficie (Ej. 120.00)[cite: 733].
     * - Licencias Comerciales: Aquí envías los m2 del local o el monto de inversión[cite: 742].
     * * Puede ser nulo si el trámite es solo una Cuota Fija simple.
     */
    private BigDecimal baseCalculo;

    /**
     * Parámetros dinámicos para reglas específicas del municipio.
     * Ej: "zona": "CENTRO", "giro": "HOTEL".
     */
    private Map<String, String> parametrosExtra;

    // Año fiscal para cálculos de adeudos pasados (Opcional)
    private Integer anioFiscal;
}