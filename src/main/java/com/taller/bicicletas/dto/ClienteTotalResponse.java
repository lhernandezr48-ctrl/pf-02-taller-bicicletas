package com.taller.bicicletas.dto;

import java.math.BigDecimal;

public record ClienteTotalResponse(
        String cliente,
        long cantidadOrdenes,
        BigDecimal totalEstimado
) {}