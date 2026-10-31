package com.taller.bicicletas.repository;

import com.taller.bicicletas.entity.ServicioTaller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioTallerRepository extends JpaRepository<ServicioTaller, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}