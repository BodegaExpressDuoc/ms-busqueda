package cl.duoc.bodegaexpress.busqueda.service;

import cl.duoc.bodegaexpress.busqueda.dto.ArticuloResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class BusquedaService {
    private final RestClient articulosRestClient;

    public BusquedaService(RestClient articulosRestClient) {
        this.articulosRestClient = articulosRestClient;
    }

    public List<ArticuloResponse> buscar(String nombre) {
        if (nombre == null || nombre.isBlank() || nombre.strip().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre debe contener entre 1 y 150 caracteres");
        }
        String termino = nombre.strip().toLowerCase(Locale.ROOT);
        List<ArticuloResponse> articulos;
        try {
            articulos = articulosRestClient.get().uri("/api/articulos").retrieve()
                    .body(new ParameterizedTypeReference<List<ArticuloResponse>>() {});
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar ms-articulos. Intenta nuevamente mas tarde.", exception);
        }
        if (articulos == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "ms-articulos devolvio una respuesta vacia");
        }
        return articulos.stream()
                .filter(articulo -> articulo.nombre() != null
                        && articulo.nombre().toLowerCase(Locale.ROOT).contains(termino))
                .toList();
    }
}
