package com.taller.bicicletas.dto;

import java.math.BigDecimal;

public record ServicioTallerResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal costoBase,
        boolean activo
) {}