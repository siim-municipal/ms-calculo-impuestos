INSERT INTO cat_municipios (id, nombre) VALUES (gen_random_uuid(), 'Tuxtepec');

INSERT INTO config_tarifas (
    id,
    municipio_id,
    concepto_clave,
    anio_fiscal,
    tipo_formula,
    parametros_regla
) VALUES (
     gen_random_uuid(),
     (SELECT id FROM cat_municipios LIMIT 1), -- Ajusta esto para apuntar a Tuxtepec
    'LIC_CONSTRUCCION',
    2025,
    'MATRIZ_CONSTRUCCION',
    '{
       "unidad_valor": "UMA",
       "selector_key": "subtipo",
       "reglas": {
         "HABITACIONAL": {
           "descripcion": "Casa Habitación (Art 99. Sec 9.1-9.4)",
           "modo_cobro": "FACTOR_POR_UNIDAD",
           "rangos": [
             { "min": 0, "max": 60, "valor": 0.13 },
             { "min": 61, "max": 100, "valor": 0.19 },
             { "min": 101, "max": 150, "valor": 0.23 },
             { "min": 151, "max": 999999, "valor": 0.26 }
           ]
         },
         "COMERCIAL": {
           "descripcion": "Local Comercial (Art 99. Sec 9.5-9.10)",
           "modo_cobro": "FACTOR_POR_UNIDAD",
           "rangos": [
             { "min": 0, "max": 60, "valor": 0.26 },
             { "min": 61, "max": 100, "valor": 0.52 },
             { "min": 101, "max": 150, "valor": 0.78 },
             { "min": 151, "max": 200, "valor": 1.04 },
             { "min": 201, "max": 500, "valor": 1.29 },
             { "min": 501, "max": 999999, "valor": 1.42 }
           ]
         },
         "BARDAS": {
            "descripcion": "Bardas y Muros (Art 99. Sec 9.14-9.15)",
            "modo_cobro": "ESCALONADO_EXCEDENTE",
            "limite_base": 50,
            "costo_base": 0.21,
            "costo_excedente": 0.11
         }
       }
    }'::jsonb
);