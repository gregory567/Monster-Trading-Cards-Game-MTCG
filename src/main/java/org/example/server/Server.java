package org.example.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.example.app.App;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.*;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class Server {
    private Request request;
    private Response response;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private App app;
    private int port;
    private String authenticatedUserToken;

    public Server(App app, int port) {
        setApp(app);
        setPort(port);
    }

    public void start() throws IOException {
        setServerSocket(new ServerSocket(getPort()));

        run();
    }

    private void run() {
        try {
            while (true) {
                setClientSocket(getServerSocket().accept());
                setInputStream(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
                setRequest(new Request(getInputStream(), authenticatedUserToken));
                setOutputStream(new PrintWriter(clientSocket.getOutputStream(), true));

                if (request.getPathname() == null) {
                    setResponse(new Response(
                            HttpStatus.BAD_REQUEST,
                            ContentType.TEXT,
                            ""
                    ));
                } else {
                    setResponse(getApp().handleRequest(request));

                    // Check if authentication was successful and retrieve the token
                    if (getResponse().getStatusCode() == HttpStatus.OK.getCode()) {
                        authenticatedUserToken = extractTokenFromResponse(response);
                    }
                }

                getOutputStream().write(getResponse().build());
            }
        } catch (IOException e) {
            // Log the exception or handle it based on your application's logging strategy
            e.printStackTrace();
        } finally {
            try {
                if (getOutputStream() != null) {
                    getOutputStream().close();
                }
                if (getInputStream() != null) {
                    getInputStream().close();
                }
                if (getClientSocket() != null && !getClientSocket().isClosed()) {
                    getClientSocket().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String extractTokenFromResponse(Response response) {
        try {
            // the token can be extracted from the JSON response
            JsonNode jsonData = response.parseJsonResponse();
            return jsonData.get("data").get("token").asText();
        } catch (JsonProcessingException e) {
            handleException(e);
            return null;
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