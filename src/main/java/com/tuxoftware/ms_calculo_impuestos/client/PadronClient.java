package com.tuxoftware.ms_calculo_impuestos.client;

import com.tuxoftware.ms_calculo_impuestos.config.PadronFeignConfig;
import com.tuxoftware.ms_calculo_impuestos.dto.feign.LicenciaDetalleDTO;
import com.tuxoftware.ms_calculo_impuestos.dto.response.InfoFiscalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(
        name = "ms-padron-unico",
        configuration = PadronFeignConfig.class
)
public interface PadronClient {

    @GetMapping("/api/v1/predios/{id}/valor-catastral")
    BigDecimal obtenerValorCatastral(@PathVariable("id") String id);

    @GetMapping("/api/v1//predios/{id}/info-fiscal")
    InfoFiscalDTO obtenerInfoPredio(@PathVariable("id") String id);

    @GetMapping("/api/v1/licencias/{id}/detalle")
    LicenciaDetalleDTO obtenerDetalleLicencia(@PathVariable("id") String id);

    @GetMapping("/api/v1//licencias/{id}/info-fiscal")
    InfoFiscalDTO obtenerInfoLicencia(@PathVariable("id") String id);
}