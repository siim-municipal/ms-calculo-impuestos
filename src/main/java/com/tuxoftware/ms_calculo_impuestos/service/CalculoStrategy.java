package com.tuxoftware.ms_calculo_impuestos.service;

import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;

import java.math.BigDecimal;

public interface CalculoStrategy {
    ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma);
    String getTipoFormula(); // Para identificar cuál usar
}