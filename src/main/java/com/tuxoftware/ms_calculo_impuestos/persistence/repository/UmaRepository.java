package com.tuxoftware.ms_calculo_impuestos.persistence.repository;

import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Uma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UmaRepository extends JpaRepository<Uma, UUID> {
    Optional<Uma> findByAnio(Integer anio);

    @Query("SELECT u FROM Uma u WHERE u.activo = true")
    Optional<Uma> findUmaVigente();

    Optional<Uma> findTopByOrderByAnioDesc();
}