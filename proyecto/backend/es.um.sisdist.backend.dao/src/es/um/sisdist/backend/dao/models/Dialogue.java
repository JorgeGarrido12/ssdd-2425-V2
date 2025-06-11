package es.um.sisdist.backend.dao.models;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Dialogue {
    // Atributos
    private String dialogueId;
    private DialogueEstados status;
    private List<Prompt> dialogue;
    private String nextUrl;
    private String endUrl;

    public String getDialogueId() {
        return dialogueId;
    }

    /**
     * @param id the id to set
     */
    public void setDialogueId(String id) {
        this.dialogueId = id;
    }

    public DialogueEstados getStatus() {
        return status;
    }

    public void setStatus(DialogueEstados status) {
        this.status = status;
    }

    /**
     * @return the prompts
     */
    public List<Prompt> getDialogue() {
        if (dialogue == null) {
            return List.of();
        }
        return Collections.unmodifiableList(dialogue);
    }
    public void setDialogue(List<Prompt> prompts) {
        this.dialogue = prompts;
    }
    public String getNextUrl() {
        return nextUrl;
    }
    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }
    public String getEndUrl() {
        return endUrl;
    }
    public void setEndUrl(String endUrl) {
        this.endUrl = endUrl;
    }

    @Override
    public String toString() {
        return "Dialogue{" +
                "id='" + dialogueId + "\', " +
                "status='" + status + "\', " +
                "dialogue='" + dialogue + "\', " +
                "nextUrl='" + nextUrl + "\', " +
                "endUrl='" + endUrl + "\'" +
                '}';
    }

    public void updateDialogueName(String newName){
        this.setDialogueId(newName);
        updateNextUrl();
        updateEndUrl();

    }


    public void addPrompt(Prompt prompt) {
        boolean replaced = false;
        for (int i = 0; i < this.dialogue.size(); i++) {
            if (this.dialogue.get(i).getTimestamp().equals(prompt.getTimestamp())) {
                this.dialogue.set(i, prompt);
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            this.dialogue.add(prompt);
            this.status = DialogueEstados.BUSY;
        }

        updateNextUrl();
    }

    public void updateNextUrl() {
        String timestamp = Long.toString(System.currentTimeMillis());
        this.nextUrl = "/dialogue/" + this.dialogueId + "/" + timestamp;
    }
    
    public void updateEndUrl() {
        this.endUrl = "/dialogue/" + this.dialogueId + "/end";
    }

    public void initialiseDialogue() {
        this.status = DialogueEstados.READY; 
        updateNextUrl();
        updateEndUrl();
        this.dialogue = new LinkedList<>();
    }

    public Dialogue(String id, DialogueEstados status, List<Prompt> prompts, String nextUrl,
            String endUrl) {
        this.dialogueId = id;
        this.status = status;
        this.dialogue = prompts;
        this.nextUrl = nextUrl;
        this.endUrl = endUrl;

        if (status == null) {
            initialiseDialogue();
        }
    }

    public Dialogue() {
    }
}
