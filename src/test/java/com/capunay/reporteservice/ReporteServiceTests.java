package com.capunay.reporteservice;

import com.capunay.reporteservice.dto.PageResponse;
import com.capunay.reporteservice.dto.ReporteRequest;
import com.capunay.reporteservice.dto.ReporteResponse;
import com.capunay.reporteservice.exception.ReporteNotFoundException;
import com.capunay.reporteservice.exception.ReporteYaAtendidoException;
import com.capunay.reporteservice.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReporteServiceTests {

    @Autowired
    private ReporteService reporteService;

    private ReporteRequest request(String dni, String tipo) {
        return new ReporteRequest(dni, LocalDateTime.now(), tipo, "Av. Siempre Viva 123", "Frente al parque");
    }

    @Test
    void registraUnReporteConEstadoPendiente() {
        ReporteResponse creado = reporteService.registrar(request("12345678", "domiciliaria"));

        assertThat(creado.id()).isNotNull();
        assertThat(creado.estado()).isEqualTo(1);
        assertThat(creado.tipo()).isEqualTo("DOMICILIARIA");
        assertThat(creado.fecha()).isNotNull();
    }

    @Test
    void listaReportesDeFormaPaginada() {
        for (int i = 0; i < 7; i++) {
            reporteService.registrar(request("12345678", "MALEZA"));
        }

        PageResponse<ReporteResponse> pagina = reporteService.listar(0, 5);

        assertThat(pagina.content()).hasSize(5);
        assertThat(pagina.totalElements()).isEqualTo(7);
        assertThat(pagina.totalPages()).isEqualTo(2);
        assertThat(pagina.hasNext()).isTrue();

        PageResponse<ReporteResponse> segunda = reporteService.listar(5, 5);
        assertThat(segunda.content()).hasSize(2);
        assertThat(segunda.hasNext()).isFalse();
    }

    @Test
    void obtieneReportePorIdYObtieneLosDeUnCiudadano() {
        ReporteResponse creado = reporteService.registrar(request("87654321", "DESMONTE"));

        assertThat(reporteService.obtener(creado.id()).dni()).isEqualTo("87654321");
        List<ReporteResponse> delCiudadano = reporteService.listarPorCiudadano("87654321");
        assertThat(delCiudadano).hasSize(1);
        assertThat(delCiudadano.get(0).id()).isEqualTo(creado.id());
    }

    @Test
    void atiendeUnReporteCambiandoElEstadoDeUnoACero() {
        ReporteResponse creado = reporteService.registrar(request("11223344", "MALEZA"));

        ReporteResponse atendido = reporteService.atender(creado.id());

        assertThat(atendido.estado()).isZero();
        assertThat(reporteService.obtener(creado.id()).estado()).isZero();
        assertThatThrownBy(() -> reporteService.atender(creado.id()))
                .isInstanceOf(ReporteYaAtendidoException.class);
    }

    @Test
    void lanzaExcepcionCuandoElReporteNoExiste() {
        assertThatThrownBy(() -> reporteService.obtener(999999))
                .isInstanceOf(ReporteNotFoundException.class);
    }
}
