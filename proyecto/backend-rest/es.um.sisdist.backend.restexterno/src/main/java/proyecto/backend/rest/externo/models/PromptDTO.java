package proyecto.backend.rest.externo.models;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PromptDTO {
    private String prompt;
    private String answer;
    private long timestamp;

    // Constructor vacío obligatorio para JAX-RS
    public PromptDTO() {
    }

    public PromptDTO(String prompt, String answer, long timestamp) {
        this.prompt = prompt;
        this.answer = answer;
        this.timestamp = timestamp;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
