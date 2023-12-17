package org.example.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private String authenticatedUserToken;

    public App() {

        DatabaseService databaseService = new DatabaseService();

        UserDAO userDAO = new UserDAO(databaseService.getConnection());
        UserRepository userRepository = new UserRepository(userDAO);

        setUserController(new UserController(userRepository));
    }

    public Response handleRequest(Request request) {

        try {
            switch (request.getMethod()) {
                case GET:
                    return handleGetRequest(request);
                case POST:
                    return handlePostRequest(request);
                case PUT:
                    return handlePutRequest(request);
                case DELETE:
                    return handleDeleteRequest(request);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            // Log the InterruptedException
            handleException(e);
        } catch (Exception e) {
            // Log the exception
            handleException(e);
            return internalServerErrorResponse();
        }
        return notFoundResponse();
    }

    private Response handleGetRequest(Request request) throws InterruptedException {
        if (request.getPathname().equals("/users")) {
            if (!authenticateRequest(request)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }
            testMultithreading();
            return getUserController().getUsers();
        } else if (request.getPathname().startsWith("/users/")) {
            String username = getUsernameFromPath(request.getPathname());
            if (!authenticateUser(request, username)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }
            return getUserController().getUser(username);
        }
        return notFoundResponse();
    }

    private Response handlePostRequest(Request request) {
        if (request.getPathname().equals("/users")) {
            String body = request.getBody();
            return getUserController().createUser(body);
        } else if (request.getPathname().equals("/sessions")) {
            String body = request.getBody();
            Response loginResponse = getUserController().loginUser(body);

            // Save the token if login is successful
            if (loginResponse.getStatusCode() == HttpStatus.OK.getCode()) {
                JsonNode jsonData = null;
                try {
                    jsonData = loginResponse.parseJsonResponse();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                setAuthenticatedUserToken(jsonData.get("data").get("token").asText());
            }

            return loginResponse;
        }
        return notFoundResponse();
    }

    private Response handlePutRequest(Request request) {
        if (request.getPathname().startsWith("/users/")) {
            String username = getUsernameFromPath(request.getPathname());
            if (!authenticateUser(request, username)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }
            String body = request.getBody();
            return getUserController().updateUser(username, body);
        }
        return notFoundResponse();
    }

    private Response handleDeleteRequest(Request request) {
        if (request.getPathname().startsWith("/users/")) {
            String username = getUsernameFromPath(request.getPathname());
            authenticateUser(request, username); // Add authentication check
            return getUserController().deleteUser(username);
        }
        return notFoundResponse();
    }

    private String getUsernameFromPath(String path) {
        // Extract username from path, e.g., "/users/john" -> "john"
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    private Response notFoundResponse() {
        return new Response(
                HttpStatus.NOT_FOUND,
                ContentType.JSON,
                "{ \"error\": \"Not Found\", \"data\": null }"
        );
    }

    private Response internalServerErrorResponse() {
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"error\": \"Internal Server Error\", \"data\": null }"
        );
    }

    private boolean authenticateRequest(Request request) {
        // Extract the user token from the request
        String userToken = request.getUserToken();

        // Check if the user token is null or empty
        if (userToken == null || userToken.isEmpty()) {
            return false;
        }

        // Check if the user associated with the token has the necessary permissions
        String authenticatedUsername = getAuthenticatedUsernameFromToken(userToken);
        if (!authenticatedUsername.equals("admin")) {
            return false;
        }

        return true;
    }

    private boolean authenticateUser(Request request, String username) {
        // Extract the user token from the request
        String userToken = request.getUserToken();

        // Check if the user token is null or empty
        if (userToken == null || userToken.isEmpty()) {
            return false;
        }

        // Check if the user associated with the token has the necessary permissions
        String authenticatedUsername = getAuthenticatedUsernameFromToken(userToken);
        if (!authenticatedUsername.equals("admin") && !authenticatedUsername.equals(username)) {
            return false;
        }

        return true;
    }

    private String getAuthenticatedUsernameFromToken(String userToken) {
        // extract the username from the user token
        return userToken.replace("-mtcgToken", "");
    }

    private void testMultithreading() throws InterruptedException {
        // Introduce a configurable sleep duration for testing
        Thread.sleep(2500);
    }

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }

    private void handleException(Exception e) {
        e.printStackTrace();
    }

}
