package com.tuxoftware.ms_calculo_impuestos.service.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuxoftware.ms_calculo_impuestos.dto.request.SolicitudCalculo;
import com.tuxoftware.ms_calculo_impuestos.dto.response.ResultadoCalculo;
import com.tuxoftware.ms_calculo_impuestos.persistence.entity.Tarifa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ImpuestosStrategiesTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final BigDecimal VALOR_UMA_2025 = new BigDecimal("108.57");

    private PorcentajeConMinimoStrategy predialStrategy;
    private CalculoRangosAguaStrategy aguaStrategy;
    private MatrizConstruccionStrategy construccionStrategy;

    @BeforeEach
    void setUp() {
        RangosConExcedenteStrategy trasladoStrategy = new RangosConExcedenteStrategy();
        predialStrategy = new PorcentajeConMinimoStrategy();
        aguaStrategy = new CalculoRangosAguaStrategy();
        construccionStrategy = new MatrizConstruccionStrategy();
    }

    @Test
    @DisplayName("Predial: Debe aplicar mínimo de UMA (redondeado a 2 decimales)")
    void testPredialAplicaMinimo() throws JsonProcessingException {
        String jsonConfig = "{ \"tasa\": 0.005, \"minimo_uma\": 4.6, \"base\": \"VALOR_CATASTRAL\" }";

        SolicitudCalculo solicitud = new SolicitudCalculo();
        solicitud.setBaseCalculo(new BigDecimal("10000.00"));

        ResultadoCalculo resultado = predialStrategy.calcular(solicitud, mockTarifa(jsonConfig), VALOR_UMA_2025);

        // 4.6 * 108.57 = 499.422 -> Redondeado a 499.42
        BigDecimal esperado = new BigDecimal("499.42");

        // Usamos compareTo para evitar error por escala (499.42 vs. 499.4200)
        assertEquals(0, esperado.compareTo(resultado.getTotal()),
                "El total debe ser 499.42 (4.6 UMA redondeado)");
    }

    @ParameterizedTest
    @CsvSource({
            "50.00, 0.30, 1628.55",  // 50 * 0.30 * 108.57 = 1628.55
            "50.01, 0.36, 1954.65"   // 50.01 * 0.36 * 108.57 = 1954.6508 -> 1954.65
    })
    @DisplayName("Agua: Verifica cambio de tarifa en límite exacto")
    void testAguaLimites(String consumoStr, String factorEsperado, String totalEsperado) throws JsonProcessingException {
        String jsonConfig = """
            {
               "rangos": [
                 {"min": 0, "max": 50, "costoUnitario": 0.30},
                 {"min": 50.01, "max": 100, "costoUnitario": 0.36}
               ]
            }
        """;

        SolicitudCalculo solicitud = new SolicitudCalculo();
        solicitud.setBaseCalculo(new BigDecimal(consumoStr));

        ResultadoCalculo resultado = aguaStrategy.calcular(solicitud, mockTarifa(jsonConfig), VALOR_UMA_2025);

        BigDecimal esperado = new BigDecimal(totalEsperado);

        assertEquals(0, esperado.compareTo(resultado.getTotal()),
                "El cálculo del agua falló para consumo: " + consumoStr);

    }

    @Test
    @DisplayName("Construcción: Habitacional < 60m2 (Redondeado)")
    void testConstruccionHabitacionalMenor60() throws JsonProcessingException {
        String jsonConfig = """
            {
               "selector_key": "subtipo",
               "reglas": {
                 "HABITACIONAL": {
                   "modo_cobro": "FACTOR_POR_UNIDAD",
                   "rangos": [ { "min": 0, "max": 60, "valor": 0.13 } ]
                 }
               }
            }
        """;

        SolicitudCalculo solicitud = new SolicitudCalculo();
        solicitud.setBaseCalculo(new BigDecimal("50.00"));
        solicitud.setParametrosExtra(Map.of("subtipo", "HABITACIONAL"));

        // Cálculo: 50 * 0.13 * 108.57 = 705.705 -> Redondeado: 705.71
        BigDecimal esperado = new BigDecimal("705.71");

        ResultadoCalculo resultado = construccionStrategy.calcular(solicitud, mockTarifa(jsonConfig), VALOR_UMA_2025);

        assertEquals(0, esperado.compareTo(resultado.getTotal()),
                "El cálculo de construcción debe redondearse a 2 decimales (705.71)");
    }

    private Tarifa mockTarifa(String json) throws JsonProcessingException {
        JsonNode node = mapper.readTree(json);
        Tarifa t = new Tarifa();
        t.setParametrosRegla(node);
        t.setDescripcion("Tarifa Mock");
        t.setClaveConcepto("TEST");
        return t;
    }
}