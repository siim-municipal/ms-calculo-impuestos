package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuxoftware.ms_calculo_impuestos.dto.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.rules.ConfigRangos;
import com.tuxoftware.ms_calculo_impuestos.dto.rules.Rango;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import com.tuxoftware.ms_calculo_impuestos.service.CalculoStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CalculoRangosAguaStrategy implements CalculoStrategy {

    // ObjectMapper es thread-safe, se puede instanciar aquí o inyectar
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ResultadoCalculo calcular(SolicitudCalculo solicitud, Tarifa tarifa, BigDecimal valorUma) {
        // 1. Convertir el JSONB a objeto Java (ConfigRangos)
        ConfigRangos config = mapper.convertValue(tarifa.getParametrosRegla(), ConfigRangos.class);

        // 2. Obtener base gravable (m3 consumidos)
        BigDecimal consumo = solicitud.getBaseCalculo();

        if (consumo == null) {
            throw new IllegalArgumentException("Se requiere 'baseCalculo' (m3) para calcular agua potable");
        }

        BigDecimal factorUma = null;

        // 3. Buscar en qué rango cae el consumo
        // El PDF (Art 74) define rangos como "De 1 a 50", "De 50.01 a 100", etc.
        for (Rango r : config.getRangos()) {
            // Lógica: consumo >= min AND consumo <= max
            if (consumo.compareTo(r.getMin()) >= 0 && consumo.compareTo(r.getMax()) <= 0) {
                factorUma = r.getCostoUnitario();
                break;
            }
        }

        // Validación de seguridad: ¿Qué pasa si el consumo excede el rango máximo configurado?
        BigDecimal total = determineWaterExpense(valorUma, factorUma, consumo);

        // 6. Construir respuesta completa
        return ResultadoCalculo.builder()
                .claveConcepto(tarifa.getClaveConcepto())
                .descripcion(tarifa.getDescripcion()) // ¡Importante para que el frontend sepa qué es!
                .subtotal(total)
                .total(total)
                .metodoCalculo("AGUA_RANGOS")
                .detalles(String.format("Consumo: %s m3. Tarifa aplicada: %s UMA/m3 (Rango detectado)", consumo, factorUma))
                .build();
    }

    private static BigDecimal determineWaterExpense(BigDecimal valorUma, BigDecimal factorUma, BigDecimal consumo) {
        if (factorUma == null) {
            throw new RuntimeException("El consumo de " + consumo + " m3 está fuera de los rangos configurados en la tarifa.");
        }

        // 4. Calcular: (FactorUMA * ValorUMA) * Consumo
        // Según PDF Art 74: "Costo en UMA por M3" -> El factor aplica a CADA metro cúbico.
        BigDecimal costoPorMetroCubico = factorUma.multiply(valorUma);
        BigDecimal total = costoPorMetroCubico.multiply(consumo);

        // 5. Redondeo a 2 decimales (Moneda)
        total = total.setScale(2, RoundingMode.HALF_UP);
        return total;
    }

    @Override
    public String getTipoFormula() { return "AGUA_RANGOS"; }
}
