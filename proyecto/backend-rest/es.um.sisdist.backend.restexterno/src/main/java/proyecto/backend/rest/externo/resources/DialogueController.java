package proyecto.backend.rest.externo.resources;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;


import proyecto.backend.rest.externo.impl.AppLogicImpl;
import proyecto.backend.rest.externo.models.DialogueDTO;
import proyecto.backend.rest.externo.models.DialogueDTOUtils;
import proyecto.backend.rest.externo.models.PromptDTO;
import proyecto.backend.rest.externo.models.PromptDTOUtils;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import proyecto.backend.rest.externo.service.GrpcDialogueService;

@Path("/u/{userId}/dialogue")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DialogueController {

    @Inject
    GrpcDialogueService grpcDialogueService;

    AppLogicImpl impl = AppLogicImpl.getInstance();

    // Obtener diálogo → usa AppLogicImpl
    @GET
    @Path("/{dialogueId}")
    public Response getDialogue(@PathParam("userId") String userId,
                                @PathParam("dialogueId") String dialogueId) {
        Dialogue d = impl.getDialogue(userId, dialogueId);
        if (d == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        DialogueDTO dto = DialogueDTOUtils.toDTO(d);
        return Response.ok(dto).build();
    }

    // Enviar prompt → /next → ESTE SÍ VA POR GRPC → OK
    @POST
    @Path("/{dialogueId}/next")
    public Response sendPrompt(@PathParam("userId") String userId,
                               @PathParam("dialogueId") String dialogueId,
                               PromptDTO promptDto) {
        Prompt p = PromptDTOUtils.fromDTO(promptDto);

        // Generar timestamp actual en el backend
        long timestamp = System.currentTimeMillis();
        p.setTimestamp(timestamp);

        // Generar nextUrl con token nuevo
        String nextUrl = "/u/" + userId + "/dialogue/" + dialogueId + "/next/" + System.currentTimeMillis();

        // GRPC → solo se envía el prompt y timestamp
        boolean accepted = grpcDialogueService.sendPrompt(
            userId, dialogueId, p.getPrompt(), String.valueOf(timestamp)  // Aquí usas el timestamp que acabas de generar
        );

        if (accepted) {
            return Response.status(Response.Status.CREATED).header("Location", nextUrl).build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    // Terminar diálogo → /end → usa AppLogicImpl
    @POST
    @Path("/{dialogueId}/end")
    public Response endDialogue(@PathParam("userId") String userId,
                                @PathParam("dialogueId") String dialogueId) {
        boolean success = impl.updateDialogueEstado(userId, dialogueId, DialogueEstados.FINISHED);

        if (success) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    // Crear diálogo → POST /u/{userId}/dialogue → usa AppLogicImpl
    @POST
    public Response createDialogue(@PathParam("userId") String userId, DialogueDTO dialogueDto) {
        Dialogue d = new Dialogue();
        d.setDialogueId(dialogueDto.getDialogueId());
        d.setStatus(DialogueEstados.READY);

        // Generar nextUrl con token inicial
        String nextUrl = "/u/" + userId + "/dialogue/" + d.getDialogueId() + "/next/" + System.currentTimeMillis();
        d.setNextUrl(nextUrl);

        // Generar endUrl
        String endUrl = "/u/" + userId + "/dialogue/" + d.getDialogueId() + "/end";
        d.setEndUrl(endUrl);

        boolean success = impl.createDialogue(userId, d);

        if (success)
            return Response.status(Response.Status.CREATED).build();
        else
            return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
