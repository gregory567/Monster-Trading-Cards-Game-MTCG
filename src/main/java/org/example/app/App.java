package org.example.app;

import lombok.Setter;
import lombok.AccessLevel;
import org.example.app.controllers.UserController;
import org.example.app.services.UserService;
import org.example.server.Request;
import org.example.server.Response;
import org.example.server.ServerApp;
import org.example.http.ContentType;
import org.example.http.HttpStatus;

public class App implements ServerApp {
    @Setter(AccessLevel.PRIVATE)
    private UserController userController;

    public App() {
        setUserController(new UserController(new UserService()));
    }

    public Response handleRequest(Request request) {


        switch (request.getMethod()) {
            case GET: {
                if (request.getPathname().equals("/users")) {
                    return this.userController.getUsers();
                }
            }
        }

        return new Response(
                HttpStatus.NOT_FOUND,
                ContentType.JSON,
                "{ \"error\": \"Not Found\", \"data\": null }"
        );
    }
}
