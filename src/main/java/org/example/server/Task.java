package org.example.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import org.example.app.App;
import org.example.http.ContentType;
import org.example.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Getter
@Setter
public class Task implements Runnable {
    private Socket clientSocket;
    private App app;
    private String authenticatedUserToken;

    public Task(Socket clientSocket, App app) {
        this.clientSocket = clientSocket;
        this.app = app;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            Request request = new Request(reader, authenticatedUserToken);
            Response response;

            if (request.getPathname() == null) {
                response = new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.TEXT,
                        ""
                );
            } else {
                response = app.handleRequest(request);

                // Check if authentication was successful and retrieve the token
                if (response.getStatusCode() == HttpStatus.OK.getCode()) {
                    authenticatedUserToken = extractTokenFromResponse(response);
                }
            }

            writer.write(response.build());
            // flush the stream to ensure data is sent immediately
            writer.flush();
        } catch (IOException e) {
            handleException(e);
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            handleException(e);
        }
    }

    private String extractTokenFromResponse(Response response) {
        try {
            // the token can be extracted from the JSON response
            JsonNode jsonData = response.parseJsonResponse();
            return jsonData.get("data").get("token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to extract token from response", e);
        }
    }

    private void handleException(IOException e) {
        // Handle the exception (e.g., log it)
        e.printStackTrace();
    }
}
