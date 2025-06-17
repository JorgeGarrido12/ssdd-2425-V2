package proyecto.backend.rest.externo.resources;


import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.LogDTO;

import proyecto.backend.rest.externo.impl.AppLogicImpl;
import proyecto.backend.rest.externo.models.DialogueDTO;
import proyecto.backend.rest.externo.models.DialogueDTOUtils;
import proyecto.backend.rest.externo.models.UserFullDTO;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/u/{userId}")
@Produces(MediaType.APPLICATION_JSON)
public class UserController {

    AppLogicImpl impl = AppLogicImpl.getInstance();

    // Obtener perfil completo del usuario
    @GET
    public Response getUserProfile(@PathParam("userId") String userId) {
        var userOpt = impl.getUserById(userId);
        if (userOpt.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();

        var stats = impl.getUsageStats(userId);
        List<Dialogue> dialogueList = impl.getAllDialoguesForUser(userId);

        List<DialogueDTO> dialogues = new ArrayList<>();
        for (Dialogue d : dialogueList) {
            dialogues.add(DialogueDTOUtils.toDTO(d));
        }

        UserFullDTO dto = new UserFullDTO();
        dto.setId(userOpt.get().getId());
        dto.setEmail(userOpt.get().getEmail());
        dto.setName(userOpt.get().getName());
        dto.setToken(userOpt.get().getToken());
        dto.setVisits(userOpt.get().getVisits());
        dto.setDialogues(dialogues);
        dto.setStats(stats);

        return Response.ok(dto).build();
    }

    // Obtener logs del usuario
    @GET
    @Path("/logs")
    public Response getLogs(@PathParam("userId") String userId) {
        List<LogDTO> logs = impl.getLogsForUser(userId);
        return Response.ok(logs).build();
    }

    // Eliminar un log
    @DELETE
    @Path("/logs/{dialogueId}")
    public Response deleteLog(@PathParam("userId") String userId,
                              @PathParam("dialogueId") String dialogueId) {
        boolean deleted = impl.deleteLog(userId, dialogueId);
        if (deleted)
            return Response.ok().build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }
}
