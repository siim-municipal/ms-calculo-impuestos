package com.tuxoftware.ms_calculo_impuestos.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class PadronFeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new PadronErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                // Obtenemos el token de la petición original (que llegó al Controller de Calculo)
                String authHeader = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);

                // Si existe, se lo pasamos a la petición de Feign hacia Padrón
                if (authHeader != null) {
                    requestTemplate.header(HttpHeaders.AUTHORIZATION, authHeader);
                }
            }
        };
    }
}