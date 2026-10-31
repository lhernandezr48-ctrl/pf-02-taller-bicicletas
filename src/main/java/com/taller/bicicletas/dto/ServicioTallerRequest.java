package com.taller.bicicletas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ServicioTallerRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción no puede pasar de 255 caracteres")
        String descripcion,

        @NotNull(message = "El costo base es obligatorio")
        @DecimalMin(value = "0.01", message = "El costo base debe ser mayor que cero")
        BigDecimal costoBase
) {}