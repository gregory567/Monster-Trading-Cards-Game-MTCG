package org.example.server;

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

    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private Socket clientSocket;
    private App app;

    public Task(Socket clientSocket, App app) {
        setClientSocket(clientSocket);
        setApp(app);
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            setInputStream(reader);
            setOutputStream(writer);

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
            if (outputStream != null) {
                outputStream.close();
                System.out.println("OutputStream closed");
            }
            if (inputStream != null) {
                inputStream.close();
                System.out.println("InputStream closed");
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("ClientSocket closed");
            }
        } catch (IOException e) {
            handleException(e);
        }
    }

    private String getAuthenticatedUsernameFromToken(String userToken) {
        // extract the username from the user token
        return userToken != null ? userToken.replace("-mtcgToken", "") : null;
    }

    private void handleException(Exception e) {
        // Log the exception or handle it based on your application's logging strategy
        e.printStackTrace();
    }
}
