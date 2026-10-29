package com.capunay.reporteservice.controller;

import com.capunay.reporteservice.dto.PageResponse;
import com.capunay.reporteservice.dto.ReporteRequest;
import com.capunay.reporteservice.dto.ReporteResponse;
import com.capunay.reporteservice.service.ReporteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reportes")
@Validated
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /**
     * Registra un reporte de basura. El estado inicial siempre es 1 (pendiente).
     */
    @PostMapping({"", "/"})
    public ResponseEntity<ReporteResponse> registrar(@Valid @RequestBody ReporteRequest request) {
        ReporteResponse creado = reporteService.registrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    /**
     * Lista los reportes de forma paginada: offset = registros a saltar, limit = tamaño de página.
     */
    @GetMapping({"", "/"})
    public PageResponse<ReporteResponse> listar(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "El offset no puede ser negativo") int offset,
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "El limit debe ser al menos 1")
            @Max(value = 100, message = "El limit no debe superar 100") int limit) {
        return reporteService.listar(offset, limit);
    }

    /**
     * Obtiene todos los reportes registrados por un ciudadano identificado por su DNI.
     */
    @GetMapping("/ciudadano/{dni}")
    public List<ReporteResponse> listarPorCiudadano(
            @PathVariable
            @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos") String dni) {
        return reporteService.listarPorCiudadano(dni);
    }

    /**
     * Obtiene el reporte correspondiente al código consultado.
     */
    @GetMapping("/{id}")
    public ReporteResponse obtener(@PathVariable @Min(value = 1, message = "El código debe ser mayor a 0") Integer id) {
        return reporteService.obtener(id);
    }

    /**
     * Marca el reporte como atendido por Limpieza Pública (estado de 1 a 0).
     */
    @PutMapping("/{id}")
    public ReporteResponse atender(
            @PathVariable @Min(value = 1, message = "El código debe ser mayor a 0") Integer id) {
        return reporteService.atender(id);
    }
}
