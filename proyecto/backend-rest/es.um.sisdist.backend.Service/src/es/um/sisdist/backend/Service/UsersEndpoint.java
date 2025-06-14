package es.um.sisdist.backend.Service;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.Response;


import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.LogDTO;
import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.models.DialogueDTO;
import es.um.sisdist.models.DialogueDTOUtils;
import es.um.sisdist.models.PromptDTO;
import es.um.sisdist.models.PromptDTOUtils;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import es.um.sisdist.models.UserFullDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/u")
public class UsersEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    /*@GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username)
    {
        return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));
    }*/

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserFullDTO getUserFullInfo(@PathParam("username") String username)
    {
        var userOpt = impl.getUserById(username);
        if (userOpt.isEmpty())
            return null;

        var stats = impl.getUsageStats(username);

        List<DialogueDTO> dialogues = new ArrayList<>();

        // Aquí es donde AHORA sí puedes usar:
        List<Dialogue> dialogueList = impl.getAllDialoguesForUser(username);
        for (Dialogue d : dialogueList)
        {
            dialogues.add(DialogueDTOUtils.toDTO(d));
        }

        // Construir el UserFullDTO
        UserFullDTO dto = new UserFullDTO();
        dto.setId(userOpt.get().getId());
        dto.setEmail(userOpt.get().getEmail());
        dto.setName(userOpt.get().getName());
        dto.setToken(userOpt.get().getToken());
        dto.setVisits(userOpt.get().getVisits());
        dto.setDialogues(dialogues);
        dto.setStats(stats);

        return dto;
    }



    @POST
    @Path("/{username}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDialogue(@PathParam("username") String username, DialogueDTO dialogueDto)
    {
        Dialogue d = new Dialogue();
        d.setDialogueId(dialogueDto.getDialogueId());
        d.setStatus(DialogueEstados.READY);

        // Generar nextUrl con token inicial
        String nextUrl = "/u/" + username + "/dialogue/" + d.getDialogueId() + "/next/" + System.currentTimeMillis();
        d.setNextUrl(nextUrl);

        // Generar endUrl
        String endUrl = "/u/" + username + "/dialogue/" + d.getDialogueId() + "/end";
        d.setEndUrl(endUrl);

        boolean success = impl.createDialogue(username, d);

        if (success)
            return Response.status(Response.Status.CREATED).build();
        else
            return Response.status(Response.Status.BAD_REQUEST).build();
    }


    @GET
    @Path("/{username}/dialogue/{dialogueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public DialogueDTO getDialogue(@PathParam("username") String username, @PathParam("dialogueId") String dialogueId)
    {
        Dialogue d = impl.getDialogue(username, dialogueId);
        return DialogueDTOUtils.toDTO(d);
    }

    @POST
    @Path("/{username}/dialogue/{dialogueId}/next")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPrompt(@PathParam("username") String username, @PathParam("dialogueId") String dialogueId, PromptDTO promptDto)
    {
        Prompt p = PromptDTOUtils.fromDTO(promptDto);

        // Generar timestamp actual en el backend
        long timestamp = System.currentTimeMillis();
        p.setTimestamp(timestamp);  // IMPORTANTE → el backend pone el timestamp aquí

        String nextUrl = "/u/" + username + "/dialogue/" + dialogueId + "/next/" + System.currentTimeMillis();
        
        boolean success = impl.addPrompt(username, dialogueId, nextUrl, p);

        if (success){
            Dialogue updated = impl.getDialogue(username, dialogueId);
            return Response.status(Response.Status.CREATED).entity(DialogueDTOUtils.toDTO(updated)).build();
        }
        else
            return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/{username}/dialogue/{dialogueId}/end")
    public Response endDialogue(@PathParam("username") String username, @PathParam("dialogueId") String dialogueId)
    {
        boolean success = impl.updateDialogueEstado(username, dialogueId, DialogueEstados.FINISHED);

        if (success)
            return Response.ok().build();
        else
            return Response.status(Response.Status.NO_CONTENT).build();
    }


    //Endpoints para el tema de los logs
    @GET
    @Path("/{username}/logs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LogDTO> getLogs(@PathParam("username") String username)
    {
        List<LogDTO> logs = impl.getLogsForUser(username);
        return logs;
    }

    @DELETE
    @Path("/{username}/logs/{dialogueId}")
    public Response deleteLog(@PathParam("username") String username, @PathParam("dialogueId") String dialogueId)
    {
        boolean success = impl.deleteLog(username, dialogueId);

        if (success)
            return Response.ok().build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }



}
