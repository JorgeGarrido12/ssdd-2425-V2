package es.um.sisdist.backend.grpc.impl;

import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;
import io.grpc.stub.StreamObserver;
import es.um.sisdist.backend.dao.user.IUserDAO;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Dialogues extends Thread {
    private PromptRequest request;
    private StreamObserver<PromptResponse> responseObserver;
    private IUserDAO dao;

    public Dialogues(PromptRequest request, StreamObserver<PromptResponse> responseObserver, IUserDAO dao) {
        super();
        this.request = request;
        this.responseObserver = responseObserver;
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            long timestamp = Long.parseLong(request.getTimestamp());
            Prompt promptMensaje = new Prompt(request.getPrompt(), "", timestamp);

            // Llamar a /prompt
            String token = enviarLlamaChat(promptMensaje.getPrompt());
            if (token.equals("0")) {
                throw new Exception("Failed to obtain token from LlamaChat");
            }

            // Hacer polling a /response/{token}
            String respuesta = getLlamaChatResponse(token);

            // Guardar respuesta
            promptMensaje.setAnswer(respuesta);
            dao.addPromptRespuesta(request.getUserId(), request.getDialogueId(), promptMensaje);

            // Poner diálogo en READY
            dao.updateDialogueEstado(request.getUserId(), request.getDialogueId(), DialogueEstados.READY);

            // Notificar OK
            responseObserver.onNext(
                PromptResponse.newBuilder()
                    .setSuccess(true)
                    .build()
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }

    private String enviarLlamaChat(String prompt) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://ssdd-llamachat:5020/prompt"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"prompt\":\"" + prompt + "\"}"))
                .build();

        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() == 202) {
                List<String> location = httpResponse.headers().allValues("Location");
                if (!location.isEmpty()) {
                    String token = location.get(0).split("/")[2];
                    return token;
                }
            } else if (httpResponse.statusCode() == 102) {
                // El servicio aún no está listo → podrías hacer retry si quieres
                System.out.println("LlamaChat not ready (102 Processing).");
            }
            return "0";
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "0";
        }
    }

    private String getLlamaChatResponse(String token) {
        int num = 0;
        boolean logrado = false;
        String respuesta = "";
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://ssdd-llamachat:5020/response/" + token))
                .header("Accept", "*/*")
                .build();

        while (num < 100 && !logrado) {
            try {
                HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (httpResponse.statusCode() == 200) {
                    Pattern patron = Pattern.compile("\"answer\":\\s*\"(.*?)\"");
                    Matcher matcher = patron.matcher(httpResponse.body());
                    if (matcher.find()) {
                        respuesta = matcher.group(1);
                        logrado = true;
                    }
                } else {
                    Thread.sleep(1000);
                }
                num++;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        return respuesta;
    }
}
