package es.um.sisdist.backend.dao.models;


public class LogDTO
{
    private String dialogueId;
    private String dialogueJson;
    private long timestamp;

    public LogDTO() {
    }

    public LogDTO(String dialogueId, String dialogueJson, long timestamp) {
        this.dialogueId = dialogueId;
        this.dialogueJson = dialogueJson;
        this.timestamp = timestamp;
    }

    public String getDialogueId() {
        return dialogueId;
    }

    public void setDialogueId(String dialogueId) {
        this.dialogueId = dialogueId;
    }

    public String getDialogueJson() {
        return dialogueJson;
    }

    public void setDialogueJson(String dialogueJson) {
        this.dialogueJson = dialogueJson;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
