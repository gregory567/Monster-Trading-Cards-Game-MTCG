package org.example.app;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;
import org.example.app.controllers.UserController;
import org.example.app.services.UserService;
import org.example.app.daos.UserDAO;
import org.example.app.repositories.UserRepository;
import org.example.app.services.DatabaseService;
import org.example.server.Request;
import org.example.server.Response;
import org.example.server.ServerApp;
import org.example.http.ContentType;
import org.example.http.HttpStatus;

import java.sql.Connection;

@AllArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class App implements ServerApp {

    private UserController userController;

    public App() {

        DatabaseService databaseService = new DatabaseService();

        UserDAO userDAO = new UserDAO(databaseService.getConnection());
        UserRepository userRepository = new UserRepository(userDAO);

        setUserController(new UserController(userRepository));
    }

    public Response handleRequest(Request request) {

        try {
            switch (request.getMethod()) {
                case GET: {
                    if (request.getPathname().equals("/users")) {
                        // Introduce a configurable sleep duration for testing
                        testMultithreading();
                        return getUserController().getUsers();
                    }
                }
                case POST: {
                    if (request.getPathname().equals("/users")) {
                        String body = request.getBody();
                        return getUserController().createUser(body);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            // Log the InterruptedException
            handleException(e);
        } catch (Exception e) {
            // Log the exception
            handleException(e);
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

    private void testMultithreading() throws InterruptedException {
        // Introduce a configurable sleep duration for testing
        Thread.sleep(2500);
    }

    private void handleException(Exception e) {
        e.printStackTrace();
    }
    
}
