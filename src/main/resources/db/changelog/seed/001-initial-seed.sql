-- 1. Insertar Municipio
INSERT INTO cat_municipios (id, nombre, rfc)
VALUES (gen_random_uuid(), 'Tuxtepec', 'MTS850101H2O');

-- 2. Tarifa Agua Potable
INSERT INTO config_tarifas (
    id,
    municipio_id,
    concepto_clave,
    anio_fiscal,
    descripcion,
    tipo_formula,
    parametros_regla)
VALUES (
           gen_random_uuid(),
           (SELECT id FROM cat_municipios WHERE nombre = 'Tuxtepec' LIMIT 1),
    'AGUA_DOMESTICO',
    2025,
    'Servicio de Agua Medido Doméstico (Art. 74)',
    'RANGOS_AGUA',
    '{
        "unidad_medida": "M3",
        "factor_base": "UMA",
        "rangos": [
            {"min": 0, "max": 50, "cuota": 0.30},
            {"min": 50.01, "max": 100, "cuota": 0.36},
            {"min": 100.01, "max": 200, "cuota": 0.41},
            {"min": 200.01, "max": 999999, "cuota": 0.44}
        ]
    }'::jsonb
);

-- 3. Impuesto Traslado de Dominio
INSERT INTO config_tarifas (
    id,
    municipio_id,
    concepto_clave,
    anio_fiscal,
    descripcion,
    tipo_formula,
    parametros_regla)
VALUES (
           gen_random_uuid(),
           (SELECT id FROM cat_municipios WHERE nombre = 'Tuxtepec' LIMIT 1),
    'IMP_TRASLADO',
    2025,
    'Impuesto s/ Traslación de Dominio (Art. 35)',
    'RANGOS_CON_EXCEDENTE',
    '{
        "moneda": "MXN",
        "tabulador": [
            {"lim_inf": 0.01, "lim_sup": 100000.00, "cuota_fija": 0, "tasa_excedente": 0.015},
            {"lim_inf": 100000.01, "lim_sup": 375000.00, "cuota_fija": 0, "tasa_excedente": 0.0175},
            {"lim_inf": 375000.01, "lim_sup": 540000.00, "cuota_fija": 0, "tasa_excedente": 0.020},
            {"lim_inf": 540000.01, "lim_sup": 1080000.00, "cuota_fija": 8250, "tasa_excedente": 0.0225},
            {"lim_inf": 1080000.01, "lim_sup": 3240000.00, "cuota_fija": 22500, "tasa_excedente": 0.025},
            {"lim_inf": 3240000.01, "lim_sup": 6480000.00, "cuota_fija": 97500, "tasa_excedente": 0.0275},
            {"lim_inf": 6480000.01, "lim_sup": 12960000.00, "cuota_fija": 202500, "tasa_excedente": 0.03},
            {"lim_inf": 12960000.01, "lim_sup": 25920000.00, "cuota_fija": 382500, "tasa_excedente": 0.0325},
            {"lim_inf": 25920000.01, "lim_sup": 999999999.99, "cuota_fija": 742500, "tasa_excedente": 0.035}
        ]
    }'::jsonb
);

-- 4. Licencias Alcoholes
INSERT INTO config_tarifas (
    id,
    municipio_id,
    concepto_clave,
    anio_fiscal,
    descripcion,
    tipo_formula,
    parametros_regla)
VALUES (
           gen_random_uuid(),
           (SELECT id FROM cat_municipios WHERE nombre = 'Tuxtepec' LIMIT 1),
    'LIC_ALCOHOL',
    2025,
    'Licencias de funcionamiento Alcoholes (Art. 119)',
    'MAPA_VALORES',
    '{
        "unidad": "UMA",
        "giros": {
            "AGENCIA_CERVEZA": {"expedicion": 24814, "revalidacion": 11260},
            "BAR_CLASE_A": {"expedicion": 350, "revalidacion": 160},
            "CANTINA": {"expedicion": 199, "revalidacion": 66},
            "DISCOTECA": {"expedicion": 3020, "revalidacion": 292}
        }
    }'::jsonb
);

-- 5. Predial Urbano
INSERT INTO config_tarifas (
    id,
    municipio_id,
    concepto_clave,
    anio_fiscal,
    descripcion,
    tipo_formula,
    parametros_regla)
VALUES (
           gen_random_uuid(),
           (SELECT id FROM cat_municipios WHERE nombre = 'Tuxtepec' LIMIT 1),
    'IMP_PREDIAL_URBANO',
    2025,
    'Impuesto Predial Urbano (Art. 17)',
    'PORCENTAJE_CON_MINIMO',
    '{
        "tasa": 0.005,
        "minimo_uma": 4.6,
        "base": "VALOR_CATASTRAL"
    }'::jsonb
);

INSERT INTO config_tarifas (
    id,
    municipio_id,
    concepto_clave,
    anio_fiscal,
    descripcion,
    tipo_formula,
    parametros_regla
) VALUES (
    gen_random_uuid(),
    (SELECT id FROM cat_municipios LIMIT 1),
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

INSERT INTO config_tarifas (
    id,
    municipio_id,
    concepto_clave,
    anio_fiscal,
    tipo_formula,
    descripcion,
    parametros_regla
) VALUES (
    gen_random_uuid(),
    (SELECT id FROM cat_municipios LIMIT 1),
    'AGUA_DOMESTICO',
    2025,
    'AGUA_RANGOS',
    'Servicio de Agua Medido (Tabla Comercial Art. 74)',
    '{
      "unidadValor": "UMA",
      "rangos": [
        { "min": 0, "max": 50, "costoUnitario": 0.30 },
        { "min": 50.01, "max": 100, "costoUnitario": 0.36 },
        { "min": 100.01, "max": 200, "costoUnitario": 0.41 },
        { "min": 200.01, "max": 400, "costoUnitario": 0.44 },
        { "min": 400.01, "max": 750.99, "costoUnitario": 0.45 },
        { "min": 751, "max": 1000, "costoUnitario": 0.49 },
        { "min": 1001.01, "max": 99999999, "costoUnitario": 0.52 }
      ]
    }'::jsonb
);