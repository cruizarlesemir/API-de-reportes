package com.capunay.reporteservice.dto;

import com.capunay.reporteservice.entity.Reporte;

import java.time.LocalDateTime;

public record ReporteResponse(
        Integer id,
        String dni,
        LocalDateTime fecha,
        String tipo,
        String direccion,
        String referencia,
        Integer estado
) {

    public static ReporteResponse from(Reporte reporte) {
        return new ReporteResponse(
                reporte.getId(),
                reporte.getDni(),
                reporte.getFecha(),
                reporte.getTipo(),
                reporte.getDireccion(),
                reporte.getReferencia(),
                reporte.getEstado()
        );
    }
}
