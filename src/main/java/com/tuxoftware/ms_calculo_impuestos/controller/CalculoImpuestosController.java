package com.tuxoftware.ms_calculo_impuestos.controller;

import com.tuxoftware.ms_calculo_impuestos.dto.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/calculos")
public class CalculoImpuestosController {

    private final CalculoService calculoService;

    public CalculoImpuestosController(CalculoService calculoService) {
        this.calculoService = calculoService;
    }

    @PostMapping("/estimar")
    public ResultadoCalculo estimarImpuesto(
            @RequestBody SolicitudCalculo solicitud,
            @AuthenticationPrincipal Jwt jwt) { // <--- Aquí Spring inyecta el token descifrado

        // 1. Extraemos el municipio_id del claim que configuraste en Keycloak
        String municipioIdStr = jwt.getClaimAsString("municipio_id");

        // Validación de seguridad defensiva
        if (municipioIdStr == null) {
            throw new RuntimeException("El usuario no tiene un municipio asignado en Keycloak");
        }

        UUID municipioId = UUID.fromString(municipioIdStr);

        // 2. Pasamos el ID al servicio para que filtre la base de datos
        return calculoService.calcularImpuesto(solicitud, municipioId);
    }
}