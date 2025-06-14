package proyecto.backend.rest.externo;

import proyecto.backend.rest.externo.auth.AuthFilter;
import proyecto.backend.rest.externo.resources.UserController;

import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class RestExternalApplication extends ResourceConfig {

    public RestExternalApplication() {
        packages("proyecto.backend.rest.externo.resources");
        register(AuthFilter.class);
        register(UserController.class);
    }

    public static void main(String[] args) throws IOException {
        URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(8180).build();
        ResourceConfig config = new RestExternalApplication();
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
        System.out.println(">>> REST externo disponible en http://localhost:8180/");
        System.in.read(); // Espera para evitar que termine el proceso
        server.shutdownNow();
    }
}
