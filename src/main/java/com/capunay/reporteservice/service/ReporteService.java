package com.capunay.reporteservice.service;

import com.capunay.reporteservice.dto.PageResponse;
import com.capunay.reporteservice.dto.ReporteRequest;
import com.capunay.reporteservice.dto.ReporteResponse;
import com.capunay.reporteservice.entity.Reporte;
import com.capunay.reporteservice.exception.ReporteNotFoundException;
import com.capunay.reporteservice.exception.ReporteYaAtendidoException;
import com.capunay.reporteservice.repository.ReporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final int ESTADO_PENDIENTE = 1;
    private static final int ESTADO_ATENDIDO = 0;

    private final ReporteRepository reporteRepository;

    public ReporteService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Transactional
    public ReporteResponse registrar(ReporteRequest request) {
        Reporte reporte = new Reporte();
        reporte.setDni(request.dni().trim());
        reporte.setFecha(request.fecha() != null ? request.fecha() : LocalDateTime.now());
        reporte.setTipo(request.tipo().trim().toUpperCase(Locale.ROOT));
        reporte.setDireccion(request.direccion().trim());
        reporte.setReferencia(normalizarReferencia(request.referencia()));
        reporte.setEstado(ESTADO_PENDIENTE);
        return ReporteResponse.from(reporteRepository.save(reporte));
    }

    public PageResponse<ReporteResponse> listar(int offset, int limit) {
        List<ReporteResponse> content = reporteRepository.findPage(offset, limit).stream()
                .map(ReporteResponse::from)
                .toList();
        return PageResponse.of(content, offset, limit, reporteRepository.count());
    }

    public ReporteResponse obtener(Integer id) {
        return ReporteResponse.from(buscarPorId(id));
    }

    public List<ReporteResponse> listarPorCiudadano(String dni) {
        return reporteRepository.findByDniOrderByIdAsc(dni.trim()).stream()
                .map(ReporteResponse::from)
                .toList();
    }

    @Transactional
    public ReporteResponse atender(Integer id) {
        Reporte reporte = buscarPorId(id);
        if (reporte.getEstado() == ESTADO_ATENDIDO) {
            throw new ReporteYaAtendidoException(id);
        }
        reporte.setEstado(ESTADO_ATENDIDO);
        return ReporteResponse.from(reporteRepository.save(reporte));
    }

    private Reporte buscarPorId(Integer id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
    }

    private String normalizarReferencia(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return null;
        }
        return referencia.trim();
    }
}
