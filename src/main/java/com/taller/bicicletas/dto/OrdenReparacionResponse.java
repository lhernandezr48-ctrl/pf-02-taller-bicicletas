package com.taller.bicicletas.dto;

import com.taller.bicicletas.entity.EstadoOrden;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OrdenReparacionResponse(
        Long id,
        String cliente,
        String bicicleta,
        LocalDate fecha,
        EstadoOrden estado,
        String observaciones,
        Long servicioId,
        String servicioNombre,
        BigDecimal costoBase,
        BigDecimal recargo,
        BigDecimal costoEstimado
) {}