package com.tuxoftware.ms_calculo_impuestos.persistence.entity;

import com.tuxoftware.ms_calculo_impuestos.enums.EstadoConstruccion;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "solicitudes_construccion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudConstruccion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "predio_id", nullable = false)
    private UUID predioId;

    // Ejemplo: OBRA_NUEVA, AMPLIACION, REGULARIZACION
    @Column(name = "tipo_tramite", nullable = false)
    private String tipoTramite;

    // Valores esperados: HABITACIONAL, COMERCIAL, INDUSTRIAL, BARDAS
    @Column(name = "subtipo_obra", nullable = false)
    private String subtipoObra;

    @Column(name = "metros_construccion", precision = 10, scale = 2)
    private BigDecimal metrosConstruccion;

    // Este es el valor de la obra declarado por el arquitecto (Base Gravable en algunos casos)
    @Column(name = "costo_presupuestado", precision = 18, scale = 2)
    private BigDecimal costoPresupuestado;

    @Column(name = "total_impuesto", precision = 18, scale = 2)
    private BigDecimal totalImpuesto;

    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus_tramite", nullable = false)
    private EstadoConstruccion estatusTramite;

    @Column(name = "dro_id")
    private UUID droId;
}
