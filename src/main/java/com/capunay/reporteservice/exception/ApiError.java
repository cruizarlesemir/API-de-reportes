package com.capunay.reporteservice.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(
        int status,
        String error,
        String mensaje,
        List<String> detalles,
        String path,
        Instant timestamp
) {

    public static ApiError of(int status, String error, String mensaje, List<String> detalles, String path) {
        return new ApiError(status, error, mensaje, detalles == null ? List.of() : detalles, path, Instant.now());
    }
}
