package com.tuxoftware.ms_calculo_impuestos.persistence.repository;

import com.tuxoftware.ms_calculo_impuestos.persistence.entity.SolicitudConstruccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SolicitudConstruccionRepository extends JpaRepository<SolicitudConstruccion, UUID> {
}
