package proyecto.backend.rest.externo.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;


import java.util.Optional;
    
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String userId = requestContext.getHeaderString("User");
        String dateHeader = requestContext.getHeaderString("Date");
        String authToken = requestContext.getHeaderString("Auth-Token");

        // Comprobamos que vengan las cabeceras necesarias
        if (userId == null || dateHeader == null || authToken == null) {
            abortWithUnauthorized(requestContext, "Missing authentication headers");
            return;
        }

        // Recuperamos el TOKEN privado del usuario desde la BD
        String userPrivateToken = getUserPrivateToken(userId);

        if (userPrivateToken == null) {
            abortWithUnauthorized(requestContext, "Invalid user");
            return;
        }

        // Construimos la cadena a hashear → URL + Date + TOKEN_PRIVADO
        String urlPath = requestContext.getUriInfo().getRequestUri().getPath();
        String stringToHash = urlPath + dateHeader + userPrivateToken;

        String calculatedHash = md5(stringToHash);

        // Comparamos con el Auth-Token recibido
        if (!calculatedHash.equalsIgnoreCase(authToken)) {
            abortWithUnauthorized(requestContext, "Invalid Auth-Token");
        }

        // Si todo OK → se permite la petición
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Unauthorized: " + message)
                    .build()
        );
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUserPrivateToken(String userId) {
        try {
            IUserDAO dao = new SQLUserDAO();
            Optional<User> userOpt = dao.getUserById(userId);

            if (userOpt.isEmpty()) {
                return null;  // Usuario no existe
            }

            return userOpt.get().getToken();  // Devolvemos el TOKEN privado
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
