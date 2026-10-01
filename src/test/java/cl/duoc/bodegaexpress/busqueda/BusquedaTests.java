package cl.duoc.bodegaexpress.busqueda;

import cl.duoc.bodegaexpress.busqueda.controller.BusquedaController;
import cl.duoc.bodegaexpress.busqueda.service.BusquedaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BusquedaTests {
    private MockRestServiceServer server;
    private MockMvc mvc;
    private static final String ARTICULOS = """
            [{"id":1,"nombre":"Caja mediana","descripcion":"Carton","precio":2500.50,"stock":15},
             {"id":2,"nombre":"Bolsa","descripcion":"Papel","precio":100.00,"stock":2}]
            """;

    @BeforeEach
    void configurar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://articulos");
        server = MockRestServiceServer.bindTo(builder).build();
        mvc = MockMvcBuilders.standaloneSetup(
                new BusquedaController(new BusquedaService(builder.build()))).build();
    }

    @Test
    void filtrarPorNombreSinDistinguirMayusculas() throws Exception {
        server.expect(requestTo("http://articulos/api/articulos"))
                .andRespond(withSuccess(ARTICULOS, MediaType.APPLICATION_JSON));
        mvc.perform(get("/api/busqueda").param("nombre", "  CAj  "))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].precio").value(2500.50))
                .andExpect(jsonPath("$[0].stock").value(15));
        server.verify();
    }

    @Test
    void sinCoincidenciasDevuelveListaVacia() throws Exception {
        server.expect(requestTo("http://articulos/api/articulos"))
                .andRespond(withSuccess(ARTICULOS, MediaType.APPLICATION_JSON));
        mvc.perform(get("/api/busqueda").param("nombre", "inexistente"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        server.verify();
    }

    @Test
    void rechazarConsultaInvalidaSinConsultarArticulos() throws Exception {
        mvc.perform(get("/api/busqueda")).andExpect(status().isBadRequest());
        for (String nombre : new String[]{"", "   ", "x".repeat(151)}) {
            mvc.perform(get("/api/busqueda").param("nombre", nombre))
                    .andExpect(status().isBadRequest());
        }
        server.verify();
    }

    @Test
    void errorRemotoDevuelve503() throws Exception {
        server.expect(requestTo("http://articulos/api/articulos")).andRespond(withServerError());
        mvc.perform(get("/api/busqueda").param("nombre", "Caja"))
                .andExpect(status().isServiceUnavailable());
        server.verify();
    }

    @Test
    void falloDeConexionDevuelve503() throws Exception {
        server.expect(requestTo("http://articulos/api/articulos"))
                .andRespond(withException(new IOException("Conexion no disponible")));
        mvc.perform(get("/api/busqueda").param("nombre", "Caja"))
                .andExpect(status().isServiceUnavailable());
        server.verify();
    }

    @Test
    void respuestaRemotaSinCuerpoDevuelve503() throws Exception {
        server.expect(requestTo("http://articulos/api/articulos")).andRespond(withNoContent());
        mvc.perform(get("/api/busqueda").param("nombre", "Caja"))
                .andExpect(status().isServiceUnavailable());
        server.verify();
    }
}
