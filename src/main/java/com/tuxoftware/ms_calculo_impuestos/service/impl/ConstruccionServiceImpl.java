package com.tuxoftware.ms_calculo_impuestos.service.impl;

import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.enums.EstadoConstruccion;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.SolicitudConstruccion;
import com.tuxoftware.ms_calculo_impuestos.persistence.repository.SolicitudConstruccionRepository;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoService;
import com.tuxoftware.ms_calculo_impuestos.service.ConstruccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConstruccionServiceImpl implements ConstruccionService {

    private final SolicitudConstruccionRepository repository;
    private final CalculoService calculoService;

    private static final String CLAVE_CONCEPTO_CONSTRUCCION = "LIC_CONSTRUCCION";

    @Transactional
    @Override
    public SolicitudConstruccion avanzarEstado(UUID id, EstadoConstruccion nuevoEstado, String municipioAlias) {
        var solicitud = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud de construcción no encontrada: " + id));

        validarTransicion(solicitud, nuevoEstado);

        // TRIGGER: Si pasa a PENDIENTE_PAGO, se calcula el impuesto automáticamente
        if (nuevoEstado == EstadoConstruccion.PENDIENTE_PAGO) {
            log.info("Calculando impuestos para solicitud construcción: {}", id);
            calcularYActualizarCosto(solicitud, municipioAlias);
        }

        log.info("Actualizando estado solicitud {}: {} -> {}", id, solicitud.getEstatusTramite(), nuevoEstado);
        solicitud.setEstatusTramite(nuevoEstado);

        return repository.save(solicitud);
    }

    private void validarTransicion(SolicitudConstruccion solicitud, EstadoConstruccion nuevoEstado) {
        // Regla de Negocio: No se puede autorizar si no se ha pagado
        if (nuevoEstado == EstadoConstruccion.AUTORIZADO && solicitud.getEstatusTramite() != EstadoConstruccion.PAGADO) {
            throw new IllegalStateException("No se puede AUTORIZAR una obra que no ha sido PAGADA.");
        }
    }

    private void calcularYActualizarCosto(SolicitudConstruccion solicitud, String municipioAlias) {
        // 1. Armamos el DTO para el motor de cálculo
        SolicitudCalculo request = new SolicitudCalculo();
        request.setClaveConcepto(CLAVE_CONCEPTO_CONSTRUCCION);
        request.setAnioFiscal(2025); // Podrías obtener LocalDate.now().getYear()
        request.setCantidad(1); // 1 licencia

        // Base de cálculo: Metros cuadrados
        request.setBaseCalculo(solicitud.getMetrosConstruccion());

        // Parametros Extra: Subtipo (HABITACIONAL/COMERCIAL) para la Matriz
        request.setParametrosExtra(Map.of(
                "subtipo", solicitud.getSubtipoObra(), // "HABITACIONAL"
                "tipo_tramite", solicitud.getTipoTramite() // "OBRA_NUEVA" (Opcional, si la tarifa lo usa)
        ));

        // 2. Invocamos al motor
        ResultadoCalculo resultado = calculoService.calcularImpuesto(request, municipioAlias);

        // 3. Persistimos el resultado
        solicitud.setTotalImpuesto(resultado.getTotal());
        log.info("Impuesto determinado para obra {}: ${}", solicitud.getId(), resultado.getTotal());
    }
}
