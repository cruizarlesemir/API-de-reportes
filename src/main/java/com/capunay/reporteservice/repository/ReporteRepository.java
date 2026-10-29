package com.capunay.reporteservice.repository;

import com.capunay.reporteservice.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    List<Reporte> findByDniOrderByIdAsc(String dni);

    @Query(value = "SELECT * FROM reportes ORDER BY id LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Reporte> findPage(@Param("offset") long offset, @Param("limit") int limit);
}
