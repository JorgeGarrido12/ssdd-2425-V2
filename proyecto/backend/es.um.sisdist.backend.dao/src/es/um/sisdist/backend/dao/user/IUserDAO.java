package es.um.sisdist.backend.dao.user;

import java.util.Optional;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.backend.dao.models.UsageStats;
import es.um.sisdist.backend.dao.models.User;

public interface IUserDAO
{
    public Optional<User> getUserById(String id);

    public Optional<User> getUserByEmail(String id);

    void createUser(User user);

    void updateVisits(String id, int visits);

    void updateToken(String id, String token);

    void deleteUser(String id); // opcional


    // Conversaciones
    boolean createDialogue(String userId, Dialogue dialogue);
    boolean updateDialogue(String userId, String dialogueId, Dialogue dialogue); // opcional (puede ser útil para restaurar)
    boolean addPrompt(String userId, String dialogueId, String nextUrl, Prompt prompt);
    boolean addPromptRespuesta(String userId, String dialogueId, Prompt prompt);
    boolean updateDialogueEstado(String userId, String dialogueId, DialogueEstados status);
    Dialogue getDialogue(String userId, String dialogueId);

    // Estadísticas (mínimo addVisits, recomendable también getUsageStats)
    boolean addVisits(String username);

    // RECOMENDADO → para UserFullDTO:
    UsageStats getUsageStats(String userId); // Añada estadísticas de uso del usuario, como número de diálogos, prompts, etc.
}
