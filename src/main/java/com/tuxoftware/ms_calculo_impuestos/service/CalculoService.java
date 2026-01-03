package com.tuxoftware.ms_calculo_impuestos.service;

import com.tuxoftware.ms_calculo_impuestos.dto.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.SolicitudCalculo;

import java.util.UUID;

public interface CalculoService {
    ResultadoCalculo calcularImpuesto(SolicitudCalculo solicitud, UUID municipioId);
}
