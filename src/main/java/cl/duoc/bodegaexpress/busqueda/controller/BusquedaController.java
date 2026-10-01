package cl.duoc.bodegaexpress.busqueda.controller;

import cl.duoc.bodegaexpress.busqueda.dto.ArticuloResponse;
import cl.duoc.bodegaexpress.busqueda.service.BusquedaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/busqueda")
public class BusquedaController {
    private final BusquedaService busquedaService;

    public BusquedaController(BusquedaService busquedaService) {
        this.busquedaService = busquedaService;
    }

    @GetMapping
    public List<ArticuloResponse> buscar(@RequestParam("nombre") String nombre) {
        return busquedaService.buscar(nombre);
    }
}
