### 🧮 MS Cálculo de Impuestos (`ms-calculo-impuesto/README.md`)

Este es el núcleo lógico. Destacamos el uso de JSONB y el patrón Strategy.

```markdown
# 🧮 MS Cálculo de Impuestos

Microservicio encargado de la **lógica fiscal y financiera** del municipio. Utiliza un motor de reglas polimórfico basado en el patrón **Strategy** y almacenamiento **JSONB** para procesar leyes de ingresos complejas (rangos, cuotas fijas, matrices de zonificación) sin recompilar el código.

![Java](https://img.shields.io/badge/Java-21-orange)
![Postgres](https://img.shields.io/badge/Postgres-JSONB-blue)
![Pattern](https://img.shields.io/badge/Pattern-Strategy-purple)

## 🧠 Lógica de Negocio

El servicio interpreta dinámicamente la configuración de la tabla `config_tarifas` según el `tipo_formula`:

1.  **CUOTA_FIJA:** Cobros simples (Ej. Copias certificadas).
2.  **AGUA_RANGOS:** Tarifas escalonadas por consumo de $m^3$.
3.  **MATRIZ_CONSTRUCCION:** Cálculo complejo por $m^2$, tipo de predio y uso de suelo.
4.  **BASURA_POR_ZONA:** Tarifas dependientes de la colonia/sector.

## 🗄️ Modelo de Datos (JSONB)

Ejemplo de regla almacenada para **Agua Potable** en PostgreSQL:

```json
{
  "unidadValor": "UMA",
  "rangos": [
    { "min": 0, "max": 50, "costoUnitario": 0.30 },
    { "min": 50.01, "max": 100, "costoUnitario": 0.36 }
  ]
}
