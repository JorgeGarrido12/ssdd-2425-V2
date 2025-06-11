package es.um.sisdist.backend.dao.models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


public class Prompt {
        private String prompt;
        private String answer;
        private long timestamp; // epoch millis


        public Prompt() {
        }

        public Prompt(String prompt, String answer, long timestamp) {
            this.prompt = prompt;
            this.answer = answer;
            this.timestamp = timestamp;

        }

        public Prompt(String prompt, long timestamp) {
        this.timestamp = timestamp;
        this.prompt = prompt;
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

        public LocalDateTime getTimestamp() {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDateTime();
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        }

    
        @Override
        public String toString() {
            return "Prompt{" +
                    "prompt='" + prompt + '\'' +
                    ", answer='" + answer + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
}
