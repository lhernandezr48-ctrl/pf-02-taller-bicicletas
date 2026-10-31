package com.taller.bicicletas.repository;

import com.taller.bicicletas.entity.EstadoOrden;
import com.taller.bicicletas.entity.OrdenReparacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenReparacionRepository extends JpaRepository<OrdenReparacion, Long> {
    List<OrdenReparacion> findByEstado(EstadoOrden estado);
    List<OrdenReparacion> findByClienteContainingIgnoreCase(String cliente);
    long countByEstado(EstadoOrden estado);
}