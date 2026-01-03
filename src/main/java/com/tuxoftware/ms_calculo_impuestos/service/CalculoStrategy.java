package com.tuxoftware.ms_calculo_impuestos.service;

import com.tuxoftware.ms_calculo_impuestos.dto.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;

import java.math.BigDecimal;

public interface CalculoStrategy {
    ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma);
    String getTipoFormula(); // Para identificar cuál usar
}