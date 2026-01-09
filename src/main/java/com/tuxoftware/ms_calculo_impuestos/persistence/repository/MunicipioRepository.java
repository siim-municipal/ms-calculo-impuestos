package com.tuxoftware.ms_calculo_impuestos.persistence.repository;

import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MunicipioRepository extends JpaRepository<Municipio, UUID> {
    @Query("SELECT m.id FROM Municipio m WHERE lower(m.nombre) = lower(:clave)")
    Optional<UUID> findIdByClaveInterna(@Param("clave") String clave);
}
