package com.tuxoftware.ms_calculo_impuestos.client;

import com.tuxoftware.ms_calculo_impuestos.config.AguaFeignConfig;
import com.tuxoftware.ms_calculo_impuestos.dto.feign.AguaConsumoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.UUID;

@FeignClient(
        name = "ms-agua",
        configuration = AguaFeignConfig.class
)
public interface AguaClient {

    @GetMapping("/api/v1/contratos/{id}/consumo-periodo")
    AguaConsumoResponse obtenerConsumoPeriodo(
            @PathVariable("id") UUID contratoId,
            @RequestParam("mes") int mes,
            @RequestParam("anio") int anio
    );
}
