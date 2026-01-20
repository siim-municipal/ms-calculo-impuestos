package com.tuxoftware.ms_calculo_impuestos.dto.feign;

import java.math.BigDecimal;
import java.util.UUID;

public record AguaConsumoResponse(
        UUID contratoId,
        UUID predioId,
        BigDecimal consumoM3,
        String tipoToma, // DOMESTICA, COMERCIAL, ETC.
        String periodo,
        boolean estimado
) {}