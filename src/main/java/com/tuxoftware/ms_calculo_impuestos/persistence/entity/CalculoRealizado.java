package com.tuxoftware.ms_calculo_impuestos.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "calculos_realizados")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculoRealizado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "clave_concepto", nullable = false)
    private String claveConcepto; // Ej: IMP_PREDIAL, LIC_ALCOHOL

    // ID del Contribuyente, Predio o Licencia al que se le cobró
    @Column(name = "referencia_id", nullable = false)
    private UUID referenciaId;

    @Column(name = "anio_fiscal")
    private Integer anioFiscal;

    // Montos desglosados
    @Column(name = "monto_base", precision = 18, scale = 2)
    private BigDecimal montoBase; // Base gravable (m2, valor catastral)

    @Column(name = "impuesto_calculado", precision = 18, scale = 2)
    private BigDecimal impuestoCalculado;

    @Column(name = "recargos", precision = 18, scale = 2)
    private BigDecimal recargos;

    @Column(name = "multas", precision = 18, scale = 2)
    private BigDecimal multas;

    @Column(name = "descuentos", precision = 18, scale = 2)
    private BigDecimal descuentos;

    @Column(name = "total_pagar", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPagar;

    // Guarda el JSON exacto de la regla que se usó en ese momento (Snapshot)
    // Vital para auditorías si la ley cambia el próximo año
    @Column(name = "desglose_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode desgloseJson;

    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;

    @Column(name = "usuario_calculo")
    private String usuarioCalculo; // Username de Keycloak
}
