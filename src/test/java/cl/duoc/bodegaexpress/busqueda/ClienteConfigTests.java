package cl.duoc.bodegaexpress.busqueda;

import cl.duoc.bodegaexpress.busqueda.config.ClienteConfig;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ClienteConfigTests {

    private HttpServer servidor;
    private final AtomicReference<String> headerRecibido = new AtomicReference<>();
    private String url;

    @BeforeEach
    void iniciar() throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            headerRecibido.set(intercambio.getRequestHeaders().getFirst("X-Gateway-Secret"));
            intercambio.sendResponseHeaders(204, -1);
            intercambio.close();
        });
        servidor.start();
        url = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void detener() {
        servidor.stop(0);
    }

    @Test
    void enviaElSecretoDelGatewayAMsArticulos() {
        new ClienteConfig().articulosRestClient(url, "s3creto").get().uri("/api/v1/articulos")
                .retrieve().toBodilessEntity();
        assertEquals("s3creto", headerRecibido.get());
    }

    @Test
    void sinSecretoNoEnviaElHeader() {
        new ClienteConfig().articulosRestClient(url, "").get().uri("/api/v1/articulos")
                .retrieve().toBodilessEntity();
        assertNull(headerRecibido.get());
    }
}
