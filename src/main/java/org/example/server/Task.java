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

            // Handle entering the lobby for battles
            if (request.getPathname().equals("/battles")) {
                handleBattleLobbyEntry();
            }

            writer.write(response.build());
            // flush the stream to ensure data is sent immediately
            //writer.flush();
        } catch (IOException e) {
            handleException(e);
        } finally {
            //closeResources();
        }
    }

    private void closeResources() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void handleBattleLobbyEntry() {
        // Check if the lobby is open; if not, wait until it is
        synchronized (app) {
            while (!app.isBattleLobbyOpen()) {
                try {
                    // Notify the app that a new user is in the lobby
                    app.notifyNewUserInLobby();
                    /*
                    When a new user enters the lobby (represented by a task/thread),
                    it notifies other waiting threads that the lobby is no longer empty,
                    potentially allowing them to proceed.
                    This is essential for coordinating multiple threads waiting for the lobby to open.
                     */

                    // Add the username to the list when a user enters the lobby
                    app.addUsernameToLobby(getAuthenticatedUsernameFromToken(authenticatedUserToken));

                    // Wait for another user to enter the lobby
                    app.wait();
                } catch (InterruptedException e) {
                    // Restore interrupted status and handle the exception
                    Thread.currentThread().interrupt();
                    handleException(e);
                }
            }
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

    private String getAuthenticatedUsernameFromToken(String userToken) {
        // extract the username from the user token
        return userToken.replace("-mtcgToken", "");
    }

    private void handleException(IOException e) {
        // Handle the exception (e.g., log it)
        e.printStackTrace();
    }

    private void handleException(InterruptedException e) {
        // Handle the exception (e.g., log it)
        e.printStackTrace();
    }
}
