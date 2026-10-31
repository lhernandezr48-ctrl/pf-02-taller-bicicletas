package com.taller.bicicletas.controller;

import com.taller.bicicletas.dto.CambioEstadoRequest;
import com.taller.bicicletas.dto.ClienteTotalResponse;
import com.taller.bicicletas.dto.IndicadorPendientesResponse;
import com.taller.bicicletas.dto.OrdenReparacionRequest;
import com.taller.bicicletas.dto.OrdenReparacionResponse;
import com.taller.bicicletas.entity.EstadoOrden;
import com.taller.bicicletas.service.OrdenReparacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenReparacionController {

    private final OrdenReparacionService service;

    public OrdenReparacionController(OrdenReparacionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenReparacionResponse crear(@Valid @RequestBody OrdenReparacionRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<OrdenReparacionResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public OrdenReparacionResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public OrdenReparacionResponse actualizar(@PathVariable Long id,
                                              @Valid @RequestBody OrdenReparacionRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public OrdenReparacionResponse cambiarEstado(@PathVariable Long id,
                                                 @Valid @RequestBody CambioEstadoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/estado/{estado}")
    public List<OrdenReparacionResponse> porEstado(@PathVariable EstadoOrden estado) {
        return service.porEstado(estado);
    }

    @GetMapping("/cliente/{cliente}")
    public List<OrdenReparacionResponse> porCliente(@PathVariable String cliente) {
        return service.porCliente(cliente);
    }

    @GetMapping("/cliente/{cliente}/total")
    public ClienteTotalResponse totalPorCliente(@PathVariable String cliente) {
        return service.totalPorCliente(cliente);
    }

    @GetMapping("/pendientes/cantidad")
    public IndicadorPendientesResponse cantidadPendientes() {
        return service.cantidadPendientes();
    }
}