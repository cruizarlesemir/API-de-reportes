package com.capunay.reporteservice.exception;

public class ReporteNotFoundException extends RuntimeException {

    public ReporteNotFoundException(Integer id) {
        super("No existe un reporte con el código " + id);
    }
}
