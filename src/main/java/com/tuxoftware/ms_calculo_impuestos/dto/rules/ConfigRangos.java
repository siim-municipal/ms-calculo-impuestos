package com.tuxoftware.ms_calculo_impuestos.dto.rules;

import lombok.Data;
import java.util.List;

@Data
public class ConfigRangos {

    // Define qué moneda usa la regla (UMA, PESOS, SM)
    private String unidadValor;

    // Lista ordenada de los rangos (se debe validar que no haya huecos)
    private List<Rango> rangos;

    // Opcional: Descripción para auditoría (ej. "Tarifas Agua 2024 Doméstico")
    private String descripcionRegla;
}
