package com.taller.bicicletas.service;

import com.taller.bicicletas.dto.ClienteTotalResponse;
import com.taller.bicicletas.dto.IndicadorPendientesResponse;
import com.taller.bicicletas.dto.OrdenReparacionRequest;
import com.taller.bicicletas.dto.OrdenReparacionResponse;
import com.taller.bicicletas.entity.EstadoOrden;
import com.taller.bicicletas.entity.OrdenReparacion;
import com.taller.bicicletas.entity.ServicioTaller;
import com.taller.bicicletas.exception.RecursoNoEncontradoException;
import com.taller.bicicletas.exception.ReglaNegocioException;
import com.taller.bicicletas.repository.OrdenReparacionRepository;
import com.taller.bicicletas.repository.ServicioTallerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrdenReparacionService {

    private final OrdenReparacionRepository ordenRepository;
    private final ServicioTallerRepository servicioRepository;

    public OrdenReparacionService(OrdenReparacionRepository ordenRepository,
                                  ServicioTallerRepository servicioRepository) {
        this.ordenRepository = ordenRepository;
        this.servicioRepository = servicioRepository;
    }

    @Transactional
    public OrdenReparacionResponse crear(OrdenReparacionRequest request) {
        ServicioTaller servicio = buscarServicioActivo(request.servicioId());
        OrdenReparacion orden = new OrdenReparacion();
        aplicar(orden, request, servicio);
        orden.setEstado(EstadoOrden.PENDIENTE);
        return aRespuesta(ordenRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenReparacionResponse> listar() {
        return ordenRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public OrdenReparacionResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public OrdenReparacionResponse actualizar(Long id, OrdenReparacionRequest request) {
        OrdenReparacion orden = buscar(id);
        if (orden.getEstado() == EstadoOrden.TERMINADA) {
            throw new ReglaNegocioException("No se puede modificar una orden terminada");
        }
        ServicioTaller servicio = buscarServicioActivo(request.servicioId());
        aplicar(orden, request, servicio);
        return aRespuesta(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenReparacionResponse cambiarEstado(Long id, EstadoOrden nuevoEstado) {
        OrdenReparacion orden = buscar(id);
        EstadoOrden actual = orden.getEstado();
        if (actual == EstadoOrden.TERMINADA && nuevoEstado != EstadoOrden.TERMINADA) {
            throw new ReglaNegocioException(
                    "Una orden terminada no puede regresar al estado " + nuevoEstado);
        }
        if (actual == nuevoEstado) {
            throw new ReglaNegocioException("La orden ya se encuentra en estado " + actual);
        }
        orden.setEstado(nuevoEstado);
        return aRespuesta(ordenRepository.save(orden));
    }

    @Transactional
    public void eliminar(Long id) {
        OrdenReparacion orden = buscar(id);
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden eliminar órdenes en estado PENDIENTE");
        }
        ordenRepository.delete(orden);
    }

    @Transactional(readOnly = true)
    public List<OrdenReparacionResponse> porEstado(EstadoOrden estado) {
        return ordenRepository.findByEstado(estado).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenReparacionResponse> porCliente(String cliente) {
        return ordenRepository.findByClienteContainingIgnoreCase(cliente.trim())
                .stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ClienteTotalResponse totalPorCliente(String cliente) {
        List<OrdenReparacion> ordenes = ordenRepository.findByClienteContainingIgnoreCase(cliente.trim());
        BigDecimal total = ordenes.stream()
                .map(OrdenReparacion::getCostoEstimado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ClienteTotalResponse(cliente.trim(), ordenes.size(), total);
    }

    @Transactional(readOnly = true)
    public IndicadorPendientesResponse cantidadPendientes() {
        return new IndicadorPendientesResponse(ordenRepository.countByEstado(EstadoOrden.PENDIENTE));
    }

    private OrdenReparacion buscar(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden con id " + id));
    }

    private ServicioTaller buscarServicioActivo(Long servicioId) {
        ServicioTaller servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el servicio con id " + servicioId));
        if (!servicio.isActivo()) {
            throw new ReglaNegocioException("El servicio '" + servicio.getNombre() + "' está desactivado");
        }
        return servicio;
    }

    private void aplicar(OrdenReparacion orden, OrdenReparacionRequest request, ServicioTaller servicio) {
        BigDecimal recargo = request.recargo() == null ? BigDecimal.ZERO : request.recargo();
        orden.setCliente(request.cliente().trim());
        orden.setBicicleta(request.bicicleta().trim());
        orden.setFecha(request.fecha());
        orden.setObservaciones(request.observaciones());
        orden.setRecargo(recargo);
        orden.setServicio(servicio);
        orden.setCostoEstimado(servicio.getCostoBase().add(recargo));
    }

    private OrdenReparacionResponse aRespuesta(OrdenReparacion o) {
        ServicioTaller s = o.getServicio();
        return new OrdenReparacionResponse(o.getId(), o.getCliente(), o.getBicicleta(), o.getFecha(),
                o.getEstado(), o.getObservaciones(), s.getId(), s.getNombre(), s.getCostoBase(),
                o.getRecargo(), o.getCostoEstimado());
    }
}