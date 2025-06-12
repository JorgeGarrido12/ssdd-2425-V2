package es.um.sisdist.backend.Service;

import java.net.URI;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

public class TestClientPrueba
{
    public static void main(String[] args) throws InterruptedException
    {
        Client client = ClientBuilder.newClient();
        WebTarget service = client.target("http://localhost:8080/Service");

        // Registrar usuario
        System.out.println("-----> REGISTER USER");
        String registerJson = "{"
            + "\"id\": \"testuser1\","
            + "\"email\": \"testuser1@um.es\","
            + "\"password\": \"test123\","
            + "\"name\": \"Test User\","
            + "\"token\": \"\","
            + "\"visits\": 0"
            + "}";

        Response response = service.path("register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(registerJson, MediaType.APPLICATION_JSON));
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + response.readEntity(String.class));
        response.close();

        // Espera 10 seg
        Thread.sleep(10000);

        // Login
        System.out.println("-----> LOGIN USER");
        String loginJson = "{"
            + "\"email\": \"testuser1@um.es\","
            + "\"password\": \"test123\""
            + "}";

        response = service.path("checkLogin")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(loginJson, MediaType.APPLICATION_JSON));
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + response.readEntity(String.class));
        response.close();

        // Espera 10 seg
        Thread.sleep(10000);

        // Crear diálogo
        System.out.println("-----> CREATE DIALOGUE");
        String dialogueJson = "{"
            + "\"dialogueId\": \"testDialogue1\""
            + "}";

        response = service.path("u").path("testuser1").path("dialogue")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(dialogueJson, MediaType.APPLICATION_JSON));
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + response.readEntity(String.class));
        response.close();

        // Espera 10 seg
        Thread.sleep(10000);

        // Enviar prompt
        System.out.println("-----> ADD PROMPT");
        String promptJson = "{"
            + "\"prompt\": \"Hola, ¿cómo estás?\","
            + "\"timestamp\": 123456789"
            + "}";

        response = service.path("u").path("testuser1").path("dialogue").path("testDialogue1").path("next")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(promptJson, MediaType.APPLICATION_JSON));
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + response.readEntity(String.class));
        response.close();

        // Espera 10 seg
        Thread.sleep(10000);

        // Consultar estado usuario
        System.out.println("-----> GET USER INFO");
        response = service.path("u").path("testuser1")
                .request(MediaType.APPLICATION_JSON)
                .get();
        System.out.println("Status: " + response.getStatus());
        System.out.println("Body: " + response.readEntity(String.class));
        response.close();

        System.out.println("-----> TEST FINISHED");


        // Auto-observador → bucle infinito haciendo GET cada 5 seg
        System.out.println("-----> STARTING AUTO-OBSERVE MODE (CTRL+C to stop)");
        while (true)
        {
            response = service.path("u").path("testuser1")
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            System.out.println("-----> AUTO-GET USER INFO:");
            System.out.println("Status: " + response.getStatus());
            System.out.println("Body: " + response.readEntity(String.class));
            response.close();

            Thread.sleep(5000); // Espera 5 seg antes de volver a hacer GET
        }

    }

    // Esta función no la usamos → puedes eliminarla si quieres
    private static URI getBaseURI()
    {
        return UriBuilder.fromUri(
                "http://localhost:8080/es.um.sisdist.RestTest").build();
    }
}