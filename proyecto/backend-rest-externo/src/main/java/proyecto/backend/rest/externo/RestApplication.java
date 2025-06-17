package proyecto.backend.rest.externo;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class RestApplication extends ResourceConfig {
    public RestApplication() {
        packages("proyecto.backend.rest.externo.resources");

        register(proyecto.backend.rest.externo.resources.UserController.class);
        register(proyecto.backend.rest.externo.resources.DialogueController.class);
        register(proyecto.backend.rest.externo.resources.PingController.class);

        // Si tienes filtros:
        register(proyecto.backend.rest.externo.auth.AuthFilter.class);
    }
}
