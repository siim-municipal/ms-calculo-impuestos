package com.tuxoftware.ms_calculo_impuestos.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface CalculoAguaService {
    BigDecimal calcularCobroAgua(String contratoIdStr, int mes, int anio, String municipioAlias);
}
