package com.tuxoftware.ms_calculo_impuestos.service;

import com.tuxoftware.ms_calculo_impuestos.enums.EstadoConstruccion;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.SolicitudConstruccion;

import java.util.UUID;

public interface ConstruccionService {

    SolicitudConstruccion avanzarEstado(UUID id, EstadoConstruccion nuevoEstado, String municipioAlias);

}
