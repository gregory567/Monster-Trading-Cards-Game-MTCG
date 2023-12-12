package org.example.server;

import org.example.app.App;
import org.example.http.ContentType;
import org.example.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Task implements Runnable {
    private Socket clientSocket;
    private App app;

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
            Request request = new Request(reader);
            Response response;

            if (request.getPathname() == null) {
                response = new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.TEXT,
                        ""
                );
            } else {
                response = app.handleRequest(request);
            }

            writer.write(response.build());
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

    private void handleException(IOException e) {
        // Handle the exception (e.g., log it)
        e.printStackTrace();
    }
}
