package com.capunay.reporteservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos para registrar un reporte. El estado no se recibe desde el cliente:
 * todo reporte nuevo se crea con estado 1 (pendiente).
 */
public record ReporteRequest(

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
        String dni,

        LocalDateTime fecha,

        @NotBlank(message = "El tipo es obligatorio")
        @Size(max = 30, message = "El tipo no debe superar los 30 caracteres")
        String tipo,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección no debe superar los 200 caracteres")
        String direccion,

        @Size(max = 255, message = "La referencia no debe superar los 255 caracteres")
        String referencia
) {
}
