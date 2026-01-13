package com.tuxoftware.ms_calculo_impuestos.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "config_tarifas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "municipio_id", nullable = false)
    private UUID municipioId;

    @Column(name = "concepto_clave", nullable = false)
    private String claveConcepto; // Ej: "AGUA_DOMESTICO"

    @Column(name = "anio_fiscal")
    private Integer anioFiscal;

    @Column(name = "descripcion")
    private String descripcion;

    // Columna CLAVE para el patrón Strategy
    // Valores esperados: "CUOTA_FIJA", "RANGOS_AGUA", "PORCENTAJE_VALOR", "MATRIZ_ZONA"
    @Column(name = "tipo_formula", nullable = false)
    private String tipoFormula;

    // Mapeo nativo de JSONB en Postgres (Hibernate 6+)
    @Column(name = "parametros_regla", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode parametrosRegla;

    @Column(name = "aplica_adicional")
    private Boolean aplicaAdicional;
}
