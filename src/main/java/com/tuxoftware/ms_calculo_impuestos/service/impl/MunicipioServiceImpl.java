package com.tuxoftware.ms_calculo_impuestos.service.impl;

import com.tuxoftware.ms_calculo_impuestos.persistence.repository.MunicipioRepository;
import com.tuxoftware.ms_calculo_impuestos.service.MunicipioService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class MunicipioServiceImpl implements MunicipioService {
    private final MunicipioRepository municipioRepository;

    public MunicipioServiceImpl(MunicipioRepository municipioRepository) {
        this.municipioRepository = municipioRepository;
    }

    @Cacheable("municipio_ids")
    @Override
    public UUID getUuidFromAlias(String alias) {
        return municipioRepository.findIdByClaveInterna(alias)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El municipio del token ('" + alias + "') no existe en la base de datos."
                ));
    }
}
