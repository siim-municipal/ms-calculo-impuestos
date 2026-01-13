package com.tuxoftware.ms_calculo_impuestos.dto.feign;

import java.math.BigDecimal;

public record LicenciaDetalleDTO(
        String id,
        BigDecimal metrosCuadrados,
        String giroClave
) {}