package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.services.UserService;
import org.example.app.repositories.UserRepository;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;
import org.example.app.models.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class UserController extends Controller {

    private UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        setUserRepository(userRepository);
    }

    // DELETE /users/:username -> löscht einen user mit dem usernamen
    // POST /users -> erstellt einen neuen user
    // PUT/PATCH /users/:username -> updated einen user mit dem usernamen
    // GET /users/:username -> gibt einen user zurück mit dem usernamen
    // GET /users -> gibt alle users zurück
    public Response getUsers() {
        try {
            List<User> userData = getUserRepository().getAll();
            String userDataJSON = getObjectMapper().writeValueAsString(userData);

            return buildJsonResponse(HttpStatus.OK, userDataJSON, null);
        } catch (JsonProcessingException e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        }
    }

    // GET /users/:username
    public Response getUserByUsername(String username) {
        try {
            // Retrieve the user data based on the username from the UserService
            User user = getUserRepository().getUserByUsername(username);

            // Check if the user is found
            if (user != null) {
                // Convert the user object to JSON
                String userDataJSON = getObjectMapper().writeValueAsString(user);
                // Return a successful response with the user data
                return buildJsonResponse(HttpStatus.OK, userDataJSON, null);
            } else {
                // Return a not found response if the user is not found
                return buildJsonResponse(HttpStatus.NOT_FOUND, null, "User not found");
            }
        } catch (JsonProcessingException e) {
            // Handle JSON processing exception
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        } catch (Exception e) {
            // Handle other exceptions
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to retrieve user");
        }
    }

    // POST /users
    public Response createUser() {
        try {
            // Implement the logic to create a new user in the UserService
            User newUser = getUserRepository().createUser("newUsername", "newPassword", 0.0, new Stack(), new Deck(), null);

            // Check if the user creation is successful
            if (newUser != null) {
                // Convert the new user object to JSON
                String newUserJSON = getObjectMapper().writeValueAsString(newUser);
                // Return a successful response with the new user data
                return buildJsonResponse(HttpStatus.CREATED, newUserJSON, null);
            } else {
                // Return a server error response if user creation fails
                return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create user");
            }
        } catch (JsonProcessingException e) {
            // Handle JSON processing exception
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        } catch (Exception e) {
            // Handle other exceptions
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create user");
        }
    }

    // DELETE /users/:username
    public Response deleteUser(String username) {
        try {
            getUserRepository().removeUser(username);
            return buildJsonResponse(HttpStatus.NO_CONTENT, null, null);
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to delete user");
        }
    }

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }
}
