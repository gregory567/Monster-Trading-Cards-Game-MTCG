package org.example.server;

import org.example.app.App;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.*;
/*
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
 */

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class Server {
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private Request request;
    private Response response;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    //private PrintWriter outputStream;
    //private BufferedReader inputStream;
    private App app;
    private int port;

    public Server(App app, int port) {
        setApp(app);
        setPort(port);
    }

    public void start() throws IOException {
        setServerSocket(new ServerSocket(getPort()));

        run();
    }

    private void run() {
        while (true) {
            try {
                Socket clientSocket = getServerSocket().accept();
                Task task = new Task(clientSocket, getApp());
                new Thread(task).start();
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    /*
    private void handleClientConnection() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(getClientSocket().getInputStream()));
                PrintWriter writer = new PrintWriter(getClientSocket().getOutputStream(), true)
        ) {
            setInputStream(reader);
            setRequest(new Request(getInputStream()));
            setOutputStream(writer);

            if (request.getPathname() == null) {
                setResponse(new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.TEXT,
                        ""
                ));
            } else {
                setResponse(getApp().handleRequest(request));
            }

            getOutputStream().write(getResponse().build());
        } catch (IOException e) {
            handleException(e);
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (getOutputStream() != null) {
                getOutputStream().close();
            }
            if (getInputStream() != null) {
                getInputStream().close();
                getClientSocket().close();
            }
        } catch (IOException e) {
            handleException(e);
        }
    }
     */

    private void handleException(IOException e) {
        // Handle the exception (e.g., log it)
        e.printStackTrace();
    }
}