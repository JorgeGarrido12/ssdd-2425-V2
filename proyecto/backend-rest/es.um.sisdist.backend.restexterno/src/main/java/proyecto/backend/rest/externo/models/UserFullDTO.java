package proyecto.backend.rest.externo.models;

import java.util.List;

import es.um.sisdist.backend.dao.models.UsageStats;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserFullDTO
{
    private String id;
    private String email;
    private String name;
    private String token;
    private int visits;

    private List<DialogueDTO> dialogues;

    private UsageStats stats;

    // Getters and setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public int getVisits() { return visits; }
    public void setVisits(int visits) { this.visits = visits; }

    public List<DialogueDTO> getDialogues() { return dialogues; }
    public void setDialogues(List<DialogueDTO> dialogues) { this.dialogues = dialogues; }

    public UsageStats getStats() { return stats; }
    public void setStats(UsageStats stats) { this.stats = stats; }
}
