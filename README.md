# 🌐 SIIM API Gateway

Este microservicio actúa como la **puerta de enlace única** (Edge Service) para el ecosistema SIIM (Sistema Integral de Ingresos Municipales). Está construido sobre **Spring Cloud Gateway** (Reactivo/WebFlux) y se encarga del enrutamiento dinámico, seguridad perimetral (OAuth2/JWT) y gestión de tráfico.

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-green)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2024.0.0-blue)
![Security](https://img.shields.io/badge/OAuth2-Resource_Server-red)

## 🚀 Características Principales

* **Enrutamiento Centralizado:** Distribuye tráfico a `ms-padron`, `ms-calculo`, etc.
* **Seguridad OAuth2:** Valida la firma de tokens JWT contra Keycloak antes de permitir el paso.
* **CORS Global:** Configuración centralizada para permitir peticiones desde el Frontend (Angular).
* **Rate Limiting:** (Opcional) Protección contra ataques de denegación de servicio.

## 🛠️ Arquitectura

```mermaid
graph LR
    Client[Frontend / App] -->|HTTPS/443| Gateway[API Gateway :8080]
    Gateway -->|Valida Token| Keycloak[Keycloak IDP]
    Gateway -->|/api/v1/sujetos| Padron[MS Padrón]
    Gateway -->|/api/calculos| Calculo[MS Cálculo]
    Gateway -->|/api/security| Security[MS Security]
