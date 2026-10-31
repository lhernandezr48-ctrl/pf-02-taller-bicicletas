package com.taller.bicicletas.service;

import com.taller.bicicletas.dto.ServicioTallerRequest;
import com.taller.bicicletas.dto.ServicioTallerResponse;
import com.taller.bicicletas.entity.ServicioTaller;
import com.taller.bicicletas.exception.RecursoNoEncontradoException;
import com.taller.bicicletas.exception.ReglaNegocioException;
import com.taller.bicicletas.repository.ServicioTallerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioTallerService {

    private final ServicioTallerRepository repository;

    public ServicioTallerService(ServicioTallerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServicioTallerResponse crear(ServicioTallerRequest request) {
        if (repository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw new ReglaNegocioException("Ya existe un servicio con el nombre: " + request.nombre());
        }
        ServicioTaller servicio = new ServicioTaller();
        aplicar(servicio, request);
        servicio.setActivo(true);
        return aRespuesta(repository.save(servicio));
    }

    @Transactional(readOnly = true)
    public List<ServicioTallerResponse> listar() {
        return repository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ServicioTallerResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public ServicioTallerResponse actualizar(Long id, ServicioTallerRequest request) {
        ServicioTaller servicio = buscar(id);
        boolean cambioNombre = !servicio.getNombre().equalsIgnoreCase(request.nombre().trim());
        if (cambioNombre && repository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw new ReglaNegocioException("Ya existe un servicio con el nombre: " + request.nombre());
        }
        aplicar(servicio, request);
        return aRespuesta(repository.save(servicio));
    }

    @Transactional
    public ServicioTallerResponse desactivar(Long id) {
        ServicioTaller servicio = buscar(id);
        servicio.setActivo(false);
        return aRespuesta(repository.save(servicio));
    }

    private ServicioTaller buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el servicio con id " + id));
    }

    private void aplicar(ServicioTaller servicio, ServicioTallerRequest request) {
        servicio.setNombre(request.nombre().trim());
        servicio.setDescripcion(request.descripcion());
        servicio.setCostoBase(request.costoBase());
    }

    private ServicioTallerResponse aRespuesta(ServicioTaller s) {
        return new ServicioTallerResponse(s.getId(), s.getNombre(), s.getDescripcion(),
                s.getCostoBase(), s.isActivo());
    }
}