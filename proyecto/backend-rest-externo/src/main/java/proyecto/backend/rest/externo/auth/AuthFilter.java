package proyecto.backend.rest.externo.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import es.um.sisdist.backend.dao.user.IUserDAO;
import es.um.sisdist.backend.dao.user.SQLUserDAO;
import es.um.sisdist.backend.dao.models.User;

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
        String path = requestContext.getUriInfo().getPath();

        //Excluir el endpoint /ping de la autenticación
        if (path.equals("ping")) {
            return; // Permite el acceso libre
        }

        String userId = requestContext.getHeaderString("User");
        String dateHeader = requestContext.getHeaderString("Date");
        String authToken = requestContext.getHeaderString("Auth-Token");

        if (userId == null || dateHeader == null || authToken == null) {
            abortWithUnauthorized(requestContext, "Missing authentication headers");
            return;
        }

        String userPrivateToken = getUserPrivateToken(userId);

        if (userPrivateToken == null) {
            abortWithUnauthorized(requestContext, "Invalid user");
            return;
        }

        String urlPath = requestContext.getUriInfo().getRequestUri().getPath();
        String stringToHash = urlPath + dateHeader + userPrivateToken;
        String calculatedHash = md5(stringToHash);

        if (!calculatedHash.equalsIgnoreCase(authToken)) {
            abortWithUnauthorized(requestContext, "Invalid Auth-Token");
        }
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
            return userOpt.map(User::getToken).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
