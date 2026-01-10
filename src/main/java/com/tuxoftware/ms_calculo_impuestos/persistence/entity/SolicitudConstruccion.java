package com.tuxoftware.ms_calculo_impuestos.persistence.entity;

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
    private UUID predioId; // Referencia a ms-padron

    // Ejemplo: OBRA_NUEVA, AMPLIACION, BARDA, DEMOLICION
    @Column(name = "tipo_tramite", nullable = false)
    private String tipoTramite;

    @Column(name = "metros_construccion", precision = 10, scale = 2)
    private BigDecimal metrosConstruccion;

    // Algunos incisos del Art. 31 cobran sobre el valor de la obra
    @Column(name = "costo_presupuestado", precision = 18, scale = 2)
    private BigDecimal costoPresupuestado;

    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud;

    // PENDIENTE_PAGO, PAGADO, AUTORIZADO, RECHAZADO
    @Column(name = "estatus_tramite")
    private String estatusTramite;

    // ID del Director Responsable de Obra (DRO) si aplica
    @Column(name = "dro_id")
    private UUID droId;
}
