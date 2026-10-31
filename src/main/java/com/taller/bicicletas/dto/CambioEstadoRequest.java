package com.taller.bicicletas.dto;

import com.taller.bicicletas.entity.EstadoOrden;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoOrden estado
) {}