package com.tuxoftware.ms_calculo_impuestos.controller;

import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoAguaService;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/calculos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cálculo de Impuestos", description = "Endpoint principal para la estimación de obligaciones fiscales")
public class CalculoImpuestosController {

    private final CalculoService calculoService;
    private final CalculoAguaService calculoAguaService;

    @Operation(summary = "Estimar pago de impuesto",
            description = "Calcula el monto a pagar basándose en el municipio del usuario logueado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cálculo exitoso",
                    content = @Content(schema = @Schema(implementation = ResultadoCalculo.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene municipio asignado en su token")
    })
    @PostMapping("/estimar")
    public ResponseEntity<ResultadoCalculo> estimarImpuesto(
            @Valid @RequestBody SolicitudCalculo solicitud,
            @AuthenticationPrincipal Jwt jwt) {

        // 2. Extracción segura del Tenant (Municipio)
        String municipioAlias = jwt.getClaimAsString("municipio_id");

        log.info("Usuario {} solicitando cálculo para concepto: {}", jwt.getSubject(), solicitud.getClaveConcepto());

        // 3. Validación de Seguridad
        if (municipioAlias == null || municipioAlias.isBlank()) {
            log.error("Intento de cálculo sin municipio_id en token. Subject: {}", jwt.getSubject());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no tiene un municipio asignado. Contacte al administrador.");
        }

        // 4. Ejecución
        ResultadoCalculo resultado = calculoService.calcularImpuesto(solicitud, municipioAlias);

        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Estimar pago de agua",
            description = "Calcula el monto a pagar basándose en el municipio del usuario logueado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cálculo exitoso",
                    content = @Content(schema = @Schema(implementation = ResultadoCalculo.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene municipio asignado en su token")
    })
    @GetMapping("/simular-agua")
    public ResponseEntity<BigDecimal> simularCalculoAgua(
            @RequestParam String contratoId,
            @RequestParam int mes,
            @RequestParam int anio,
    @AuthenticationPrincipal Jwt jwt) {

        // 2. Extracción segura del Tenant (Municipio)
        String municipioAlias = jwt.getClaimAsString("municipio_id");

        log.info("Usuario {} solicitando cálculo de agua", jwt.getSubject());

        // 3. Validación de Seguridad
        if (municipioAlias == null || municipioAlias.isBlank()) {
            log.error("Intento de cálculo sin municipio_id en token: {}", jwt.getSubject());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no tiene un municipio asignado. Contacte al administrador.");
        }

        BigDecimal total = calculoAguaService.calcularCobroAgua(contratoId, mes, anio, municipioAlias);
        return ResponseEntity.ok(total);
    }
}