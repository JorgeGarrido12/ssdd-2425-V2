package proyecto.backend.rest.externo;

import proyecto.backend.rest.externo.auth.AuthFilter;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class RestExternalApplication extends ResourceConfig {

    public RestExternalApplication() {
        // Indicar el paquete donde tienes los Resources REST
        packages("proyecto.backend.rest.externo.resources");

        // Registrar el AuthFilter → obligatorio
        register(AuthFilter.class);

    }
}
