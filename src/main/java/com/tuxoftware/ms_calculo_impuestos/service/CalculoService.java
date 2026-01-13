package com.tuxoftware.ms_calculo_impuestos.service;

import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;

public interface CalculoService {
    ResultadoCalculo calcularImpuesto(SolicitudCalculo solicitud, String municipioId);
}
