# 📊 ms-calculos-impuestos - Microservicio de Cálculos Fiscales

## 🏛️ Descripción

Microservicio especializado en el cálculo de obligaciones fiscales según la **Ley de Ingresos del Municipio de San Juan Bautista Tuxtepec 2025**. Implementa múltiples estrategias de cálculo parametrizables para cubrir todos los conceptos tributarios del municipio.

## 🎯 Características Principales

- ✅ **7 estrategias de cálculo** diferentes parametrizadas
- ✅ **Configuración dinámica** mediante JSON
- ✅ **Catálogos maestros** normalizados
- ✅ **Cache de configuraciones** con Redis
- ✅ **Auditoría completa** de cálculos ejecutados
- ✅ **Versionado por ejercicio fiscal**
- ✅ **Arquitectura basada en estrategias** (Strategy Pattern)
- ✅ **API RESTful** documentada

## 🏗️ Arquitectura del Sistema

```
ms-calculos-impuestos/
├── controllers/          # Controladores REST
├── services/            # Lógica de negocio
│   ├── estrategias/     # Implementación de estrategias
│   └── factory/         # Factory para selección de estrategias
├── repositories/        # Acceso a datos
├── models/             # DTOs, Entities, Enums
├── config/             # Configuración Spring/Redis
└── utils/              # Utilidades comunes
```

## 📋 Estrategias de Cálculo Implementadas

### 1. 🎯 **CUOTA_FIJA** - Valores Absolutos
**Descripción:** Montos fijos establecidos en la ley sin variación.

**Ejemplos:**
- Permisos y trámites específicos
- Certificaciones oficiales
- Multas por infracciones determinadas

**Configuración JSON:**
```json
{
  "tipo": "CUOTA_FIJA",
  "monto": 11.84,
  "unidad": "UMA",
  "descripcion": "Servicio de inhumación"
}
```

### 2. 📈 **ESCALONADO_RANGOS** - Tarifas Progresivas
**Descripción:** Tarifas que aumentan según rangos de consumo o valor.

**Ejemplos:**
- Agua potable por m³ consumido
- Drenaje sanitario
- Impuesto sobre traslación de dominio

**Configuración JSON:**
```json
{
  "tipo": "ESCALONADO_RANGOS",
  "unidadBase": "m3",
  "unidadValor": "UMA",
  "rangos": [
    {"min": 0, "max": 50, "tarifa": 0.30},
    {"min": 50.01, "max": 100, "tarifa": 0.36},
    {"min": 100.01, "max": null, "tarifa": 0.45}
  ]
}
```

### 3. 🏠 **MATRIZ_CONSTRUCCION** - Predial Complejo
**Descripción:** Cálculo multivariable para impuesto predial.

**Componentes:**
1. **Valor del Suelo** × Factores de ajuste
2. **Valor de Construcción** × Factores de edad/conservación
3. Aplicación de **tasas diferenciales** (0.5% urbano, 1.3% especial)
4. **Mínimo garantizado** en UMAs

**Configuración JSON:**
```json
{
  "tipo": "MATRIZ_CONSTRUCCION",
  "tasas": {
    "urbano": 0.005,
    "especial": 0.013,
    "rustico": 0.003
  },
  "minimos": {
    "urbanoUMAs": 4.6,
    "rusticoUMAs": 4.0
  },
  "factoresSuelo": {
    "forma": {"regular": 1.0, "irregular": 0.95},
    "ubicacion": {"esquina": 1.10, "interior": 0.60},
    "topografia": {"plano": 1.0, "inclinado": 0.95}
  }
}
```

### 4. 🗺️ **TARIFA_ZONAL** - Diferenciación por Área
**Descripción:** Tarifas que varían según ubicación geográfica.

**Ejemplos:**
- Servicio de basura por colonia (Tipo A, B, C)
- Iluminación pública por sector poblacional
- Mercados con tarifas diferenciadas

**Configuración JSON:**
```json
{
  "tipo": "TARIFA_ZONAL",
  "unidadValor": "UMA",
  "zonas": [
    {"colonia": "CENTRO", "tipoServicio": "A", "tarifa": 0.77},
    {"colonia": "LOMAS SAN JUAN", "tipoServicio": "B", "tarifa": 0.60},
    {"colonia": "LAS LIMAS", "tipoServicio": "C", "tarifa": 0.44}
  ]
}
```

### 5. ⚖️ **POR_UNIDAD** - Medición por Unidad
**Descripción:** Cobro basado en unidades específicas de medida.

**Ejemplos:**
- Rastro: por cabeza de ganado, por kilo
- Panteones: por servicio específico
- Mercados: por m² ocupado
- Espectáculos: por evento

**Configuración JSON:**
```json
{
  "tipo": "POR_UNIDAD",
  "conceptos": [
    {"concepto": "MATANZA_BOVINO", "unidad": "cabeza", "valor": 689488.05},
    {"concepto": "MATANZA_PORCINO", "unidad": "cabeza", "valor": 566.91},
    {"concepto": "CARNE_RES", "unidad": "kilo", "valor": 0.015}
  ]
}
```

### 6. 📊 **PORCENTAJE_BASE** - Proporcional al Valor
**Descripción:** Porcentaje aplicado sobre una base de valor.

**Ejemplos:**
- Diversiones y espectáculos (4%-6% ingresos)
- Rifas y sorteos
- Fraccionamiento (1% valor suelo+construcción)

**Configuración JSON:**
```json
{
  "tipo": "PORCENTAJE_BASE",
  "porcentaje": 4.0,
  "descripcion": "Teatros y circos - 4% sobre ingresos brutos"
}
```

### 7. 🧩 **HIBRIDO** - Combinación Múltiple
**Descripción:** Estrategias que combinan múltiples enfoques.

**Ejemplos:**
- Iluminación pública (costo total distribuido)
- Impuesto predial (matriz + mínimo garantizado)

## 🚀 API Endpoints

### POST `/api/v1/calcular`
**Calcula una obligación fiscal**

**Request:**
```json
{
  "conceptoClave": "IMP_PREDIAL",
  "parametros": {
    "colonia": "CENTRO",
    "superficieTerreno": 250,
    "superficieConstruccion": 150,
    "tipoConstruccion": "HUE3",
    "edadConstruccion": 10
  },
  "usuarioId": "USR-001"
}
```

**Response:**
```json
{
  "idCalculo": "550e8400-e29b-41d4-a716-446655440000",
  "concepto": "Impuesto Predial",
  "total": 1845.75,
  "unidad": "PESOS",
  "desglose": {
    "valorSuelo": 1200.50,
    "valorConstruccion": 2480.25,
    "tasaAplicada": "0.5%",
    "impuestoBase": 1840.38,
    "recargos": 5.37,
    "minimoAsegurado": false
  },
  "formulaUtilizada": "MATRIZ_CONSTRUCCION",
  "fechaCalculo": "2024-01-15T10:30:00"
}
```

### GET `/api/v1/conceptos`
**Lista todos los conceptos disponibles**



## 🧪 Ejemplos de Uso

### Ejemplo 1: Cálculo de Agua Potable
```java
CalculoRequest request = CalculoRequest.builder()
    .conceptoClave("DER_AGUA_POTABLE")
    .parametros(Map.of(
        "consumoM3", 75.5,
        "tipoUso", "DOMESTICO"
    ))
    .usuarioId("USR-123")
    .build();

CalculoResponse response = calculoService.calcular(request);
```

### Ejemplo 2: Cálculo de Impuesto Predial
```java
CalculoRequest request = CalculoRequest.builder()
    .conceptoClave("IMP_PREDIAL")
    .parametros(Map.of(
        "coloniaClave", "CENTRO",
        "superficieTerreno", 300.0,
        "superficieConstruccion", 180.0,
        "tipoConstruccion", "HUM4",
        "edadAnios", 15,
        "estadoConservacion", "BUENO"
    ))
    .build();
```

## 📊 Métricas y Monitoreo

### Métricas Expuestas
- `calculos_totales`: Contador de cálculos ejecutados
- `calculos_por_tipo`: Distribución por tipo de fórmula
- `tiempo_promedio_calculo`: Tiempo de respuesta
- `errores_calculo`: Errores por tipo
- `cache_hit_rate`: Eficiencia de cache

### Health Checks
```
GET /actuator/health       # Estado general
GET /actuator/metrics      # Métricas detalladas
GET /actuator/cache        # Estado de cache
GET /actuator/db           # Estado de base de datos
```
