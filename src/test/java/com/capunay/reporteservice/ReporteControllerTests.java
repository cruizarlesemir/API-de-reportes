package com.capunay.reporteservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReporteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private static final String REPORTE_JSON = """
            {
              "dni": "12345678",
              "fecha": "2026-10-29T09:30:00",
              "tipo": "DOMICILIARIA",
              "direccion": "Av. Los Álamos 456",
              "referencia": "Cerca al mercado"
            }
            """;

    private long crearReporte() throws Exception {
        String location = mockMvc.perform(post("/reportes/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REPORTE_JSON))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    void registraReporteConEstadoUno() throws Exception {
        mockMvc.perform(post("/reportes/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REPORTE_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.tipo").value("DOMICILIARIA"))
                .andExpect(jsonPath("$.fecha").value("2026-10-29T09:30:00"))
                .andExpect(jsonPath("$.estado").value(1));
    }

    @Test
    void registraReporteSinFechaYAsignaLaFechaActual() throws Exception {
        mockMvc.perform(post("/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dni": "87654321",
                                  "tipo": "MALEZA",
                                  "direccion": "Calle 1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fecha").isNotEmpty())
                .andExpect(jsonPath("$.estado").value(1));
    }

    @Test
    void listaReportesPaginados() throws Exception {
        crearReporte();
        crearReporte();

        mockMvc.perform(get("/reportes/").param("offset", "0").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.offset").value(0))
                .andExpect(jsonPath("$.limit").value(5))
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void obtieneReportePorIdYPorDni() throws Exception {
        long id = crearReporte();

        mockMvc.perform(get("/reportes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/reportes/ciudadano/{dni}", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dni").value("12345678"));
    }

    @Test
    void atiendeReporteYCambiaEstadoACero() throws Exception {
        long id = crearReporte();

        mockMvc.perform(put("/reportes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(0));

        mockMvc.perform(put("/reportes/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void devuelveBadRequestSiLosDatosSonInvalidos() throws Exception {
        mockMvc.perform(post("/reportes/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dni": "123",
                                  "tipo": "",
                                  "direccion": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    void devuelveNotFoundSiElReporteNoExiste() throws Exception {
        mockMvc.perform(get("/reportes/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }
}
