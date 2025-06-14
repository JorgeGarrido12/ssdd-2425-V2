package es.um.sisdist.backend.Service;

import jakarta.ws.rs.core.Response;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/register")
public class RegisterEndpoint {
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(UserDTO uo) {
        User newUser = UserDTOUtils.fromDTO(uo);
        newUser.setVisits(0); // el backend controla visits

        // Creamos el usuario
        impl.createUser(newUser);

        // Recuperamos el usuario desde la base de datos con su ID generado
        var userOpt = impl.getUserByEmail(newUser.getEmail());
        if (userOpt.isEmpty())
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo registrar el usuario.")
                    .build();

        UserDTO dto = UserDTOUtils.toDTO(userOpt.get());

        // Devolver DTO completo con id, name, email, token, visits
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

}