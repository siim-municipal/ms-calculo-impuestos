package com.tuxoftware.ms_calculo_impuestos.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cat_municipios")
public class Municipio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 100)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Size(max = 13)
    @Column(name = "rfc", length = 13)
    private String rfc;

    @OneToMany
    @JoinColumn(name = "municipio_id")
    private Set<Tarifa> configTarifas = new LinkedHashSet<>();

}