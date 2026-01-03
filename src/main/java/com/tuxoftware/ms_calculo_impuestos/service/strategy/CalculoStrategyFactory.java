package com.tuxoftware.ms_calculo_impuestos.service.strategy;


import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CalculoStrategyFactory {
    private final Map<String, CalculoStrategy> estrategias;

    public CalculoStrategyFactory(List<CalculoStrategy> listaEstrategias) {
        this.estrategias = listaEstrategias.stream()
                .collect(Collectors.toMap(CalculoStrategy::getTipoFormula, Function.identity()));
    }

    public CalculoStrategy getEstrategia(String tipoFormula) {
        return Optional.ofNullable(estrategias.get(tipoFormula))
                .orElseThrow(() -> new RuntimeException("No existe implementación para la fórmula: " + tipoFormula));
    }
}
