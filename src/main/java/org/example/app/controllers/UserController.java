package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.services.UserService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class UserController extends Controller {

    private UserService userService;

    public UserController(UserService userService) {
        setUserService(userService);
    }

    // DELETE /users/:username -> löscht einen user mit dem usernamen
    // POST /users -> erstellt einen neuen user
    // PUT/PATCH /users/:username -> updated einen user mit dem usernamen
    // GET /users/:username -> gibt einen user zurück mit dem usernamen
    // GET /users -> gibt alle users zurück
    public Response getUsers() {
        try {
            List<?> userData = getUserService().getUsers();
            String userDataJSON = getObjectMapper().writeValueAsString(userData);

            return buildJsonResponse(HttpStatus.OK, userDataJSON, null);
        } catch (JsonProcessingException e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        }
    }

    // GET /users/:username
    public Response getUserByUsername(String username) {
        return buildJsonResponse(HttpStatus.OK, null, null);
    }

    // POST /users
    public Response createUser() {
        return buildJsonResponse(HttpStatus.CREATED, null, null);
    }

    // DELETE /users/:username
    public Response deleteUser(String username) {
        try {
            getUserService().deleteUser(username);
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
