package com.taller.bicicletas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OrdenReparacionRequest(
        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 100, message = "El cliente no puede pasar de 100 caracteres")
        String cliente,

        @NotBlank(message = "La bicicleta es obligatoria")
        @Size(max = 100, message = "La bicicleta no puede pasar de 100 caracteres")
        String bicicleta,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @Size(max = 255, message = "Las observaciones no pueden pasar de 255 caracteres")
        String observaciones,

        @DecimalMin(value = "0.00", message = "El recargo no puede ser negativo")
        BigDecimal recargo,

        @NotNull(message = "El servicio es obligatorio")
        Long servicioId
) {}