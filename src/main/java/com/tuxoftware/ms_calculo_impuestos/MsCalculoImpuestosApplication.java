package com.tuxoftware.ms_calculo_impuestos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MsCalculoImpuestosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCalculoImpuestosApplication.class, args);
	}

}
