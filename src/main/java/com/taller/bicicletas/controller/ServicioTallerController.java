package com.taller.bicicletas.controller;

import com.taller.bicicletas.dto.ServicioTallerRequest;
import com.taller.bicicletas.dto.ServicioTallerResponse;
import com.taller.bicicletas.service.ServicioTallerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioTallerController {

    private final ServicioTallerService service;

    public ServicioTallerController(ServicioTallerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioTallerResponse crear(@Valid @RequestBody ServicioTallerRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<ServicioTallerResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ServicioTallerResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public ServicioTallerResponse actualizar(@PathVariable Long id,
                                             @Valid @RequestBody ServicioTallerRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/desactivar")
    public ServicioTallerResponse desactivar(@PathVariable Long id) {
        return service.desactivar(id);
    }
}