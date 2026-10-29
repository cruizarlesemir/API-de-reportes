package com.capunay.reporteservice.exception;

public class ReporteYaAtendidoException extends RuntimeException {

    public ReporteYaAtendidoException(Integer id) {
        super("El reporte con el código " + id + " ya fue atendido por Limpieza Pública");
    }
}
