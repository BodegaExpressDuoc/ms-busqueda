package cl.duoc.bodegaexpress.busqueda.config;

import cl.duoc.bodegaexpress.busqueda.seguridad.FiltroGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ClienteConfig {
    @Bean
    public RestClient articulosRestClient(@Value("${articulos.base-url}") String baseUrl,
                                          @Value("${gateway.secret:}") String gatewaySecret) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl).requestFactory(factory);
        // La llamada interna a ms-articulos no pasa por el API Gateway: se envía el mismo secreto
        // para que el FiltroGateway de ms-articulos la acepte.
        if (gatewaySecret != null && !gatewaySecret.isBlank()) {
            builder.defaultHeader(FiltroGateway.HEADER, gatewaySecret);
        }
        return builder.build();
    }
}
