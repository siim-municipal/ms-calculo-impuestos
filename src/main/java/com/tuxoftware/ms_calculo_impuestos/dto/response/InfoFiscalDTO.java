package com.tuxoftware.ms_calculo_impuestos.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record InfoFiscalDTO(
        UUID id,
        BigDecimal valorBase, // Valor Catastral o M2 Construcción
        String municipioAlias,     // DATO CRÍTICO PARA SEGURIDAD
        String estatus        // Ej. ACTIVO, BAJA
) {}