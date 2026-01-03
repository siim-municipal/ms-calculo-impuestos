package com.tuxoftware.ms_calculo_impuestos.persistence.repository;

import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TarifasRepository extends JpaRepository<Tarifa, UUID> {
    Optional<Tarifa> findByClaveConceptoAndMunicipioIdAndAnioFiscal(
            String claveConcepto,
            UUID municipioId,
            Integer anioFiscal
    );
}
