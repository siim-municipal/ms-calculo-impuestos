package com.tuxoftware.ms_calculo_impuestos.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cat_uma")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Uma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "anio", nullable = false, unique = true)
    private Integer anio;

    @Column(name = "valor_diario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiario;

    @Column(name = "valor_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensual;

    @Column(name = "valor_anual", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorAnual;

    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate fechaInicioVigencia; // Generalmente 1 de Febrero

    @Column(name = "fecha_fin_vigencia", nullable = false)
    private LocalDate fechaFinVigencia;   // Generalmente 31 de Enero del sig. año

    @Column(name = "activo")
    private Boolean activo;
}
