-- UMA 2024 (Datos Reales INEGI)
INSERT INTO cat_uma (id, anio, valor_diario, valor_mensual, valor_anual, fecha_inicio_vigencia, fecha_fin_vigencia, activo)
VALUES (
           gen_random_uuid(),
           2024,
           108.57,
           3300.53,
           39606.36,
           '2024-02-01',
           '2025-01-31',
           false -- Ya no es la vigente para cobros 2025, pero sirve para históricos
       );

-- UMA 2025
INSERT INTO cat_uma (id, anio, valor_diario, valor_mensual, valor_anual, fecha_inicio_vigencia, fecha_fin_vigencia, activo)
VALUES (
           gen_random_uuid(),
           2025,
           113.16,
           3439.46,
           41273.52,
           '2025-02-01',
           '2026-01-31',
           false
       );

INSERT INTO cat_uma (id, anio, valor_diario, valor_mensual, valor_anual, fecha_inicio_vigencia, fecha_fin_vigencia, activo)
VALUES (
           gen_random_uuid(),
           2026,
           117.31,
           3566.22,
           42794.64,
           '2026-02-01',
           '2027-01-31',
           true
       );