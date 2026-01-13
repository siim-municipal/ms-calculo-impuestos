package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.enums.EstadoConstruccion;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.SolicitudConstruccion;
import com.tuxoftware.ms_calculo_impuestos.persistence.repository.SolicitudConstruccionRepository;
import com.tuxoftware.ms_calculo_impuestos.service.ConstruccionService;
import com.tuxoftware.ms_calculo_impuestos.service.impl.CalculoServiceImpl;
import com.tuxoftware.ms_calculo_impuestos.service.impl.ConstruccionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConstruccionServiceTest {

    @Mock
    private SolicitudConstruccionRepository repository;

    @Mock
    private CalculoServiceImpl calculoService;

    @InjectMocks
    private ConstruccionServiceImpl service;

    @Test
    void debeCalcularImpuesto_AlPasarAPendientePago() {
        // GIVEN
        UUID id = UUID.randomUUID();
        SolicitudConstruccion solicitud = SolicitudConstruccion.builder()
                .id(id)
                .estatusTramite(EstadoConstruccion.REVISION_TECNICA)
                .metrosConstruccion(new BigDecimal("100"))
                .subtipoObra("HABITACIONAL")
                .tipoTramite("OBRA_NUEVA")
                .build();

        // Simulamos la respuesta del motor de cálculo (MatrizConstruccionStrategy)
        ResultadoCalculo resultadoMock = ResultadoCalculo.builder()
                .total(new BigDecimal("1500.00"))
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(solicitud));
        when(calculoService.calcularImpuesto(any(SolicitudCalculo.class), eq("TUXTEPEC")))
                .thenReturn(resultadoMock);
        when(repository.save(any(SolicitudConstruccion.class))).thenAnswer(i -> i.getArgument(0));

        // WHEN
        SolicitudConstruccion result = service.avanzarEstado(id, EstadoConstruccion.PENDIENTE_PAGO, "TUXTEPEC");

        // THEN
        assertEquals(EstadoConstruccion.PENDIENTE_PAGO, result.getEstatusTramite());
        assertEquals(new BigDecimal("1500.00"), result.getTotalImpuesto()); // Validamos que se guardó el costo

        // Verificamos que se llamó al servicio de cálculo con los metros correctos
        verify(calculoService).calcularImpuesto(argThat(req ->
                req.getBaseCalculo().equals(new BigDecimal("100")) &&
                        req.getParametrosExtra().get("subtipo").equals("HABITACIONAL")
        ), eq("TUXTEPEC"));
    }

    @Test
    void debeLanzarError_SiAutorizaSinPagar() {
        // GIVEN
        UUID id = UUID.randomUUID();
        SolicitudConstruccion solicitud = SolicitudConstruccion.builder()
                .id(id)
                .estatusTramite(EstadoConstruccion.PENDIENTE_PAGO) // Aún no es PAGADO
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(solicitud));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () ->
                service.avanzarEstado(id, EstadoConstruccion.AUTORIZADO, "TUXTEPEC")
        );

        verify(repository, never()).save(any());
    }
}