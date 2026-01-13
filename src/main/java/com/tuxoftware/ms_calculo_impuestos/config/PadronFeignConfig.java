package com.tuxoftware.ms_calculo_impuestos.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class PadronFeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new PadronErrorDecoder();
    }
}