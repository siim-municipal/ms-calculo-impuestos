package com.tuxoftware.ms_calculo_impuestos.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PadronErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El recurso solicitado no existe en el Padrón Único.");
            case 503, 500 -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de Padrón no está disponible momentáneamente.");
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}