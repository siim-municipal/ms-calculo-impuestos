package com.tuxoftware.ms_calculo_impuestos.dto.feign;

import java.math.BigDecimal;

public record PredioValorDTO(
        String id,
        BigDecimal valorCatastral
) {}
