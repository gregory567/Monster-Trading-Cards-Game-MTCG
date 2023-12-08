package org.example.app;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;
import org.example.app.controllers.UserController;
import org.example.app.services.UserService;
import org.example.server.Request;
import org.example.server.Response;
import org.example.server.ServerApp;
import org.example.http.ContentType;
import org.example.http.HttpStatus;

@AllArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class App implements ServerApp {

    private UserController userController;

    public App() {
        setUserController(new UserController(new UserService()));
    }

    public Response handleRequest(Request request) {

        try {
            switch (request.getMethod()) {
                case GET: {
                    if (request.getPathname().equals("/users")) {
                        return getUserController().getUsers();
                    }
                }
            }
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"Internal Server Error\", \"data\": null }"
            );
        }

        return new Response(
                HttpStatus.NOT_FOUND,
                ContentType.JSON,
                "{ \"error\": \"Not Found\", \"data\": null }"
        );
    }
}
