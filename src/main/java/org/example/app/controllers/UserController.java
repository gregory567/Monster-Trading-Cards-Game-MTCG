package org.example.app.controllers;

import org.example.app.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.server.Response;

import java.util.List;

public class UserController extends Controller {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
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
            List userData = getUserService().getUsers();
            String userDataJSON = getObjectMapper().writeValueAsString(userData);

            return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                "{ \"data\": " + userDataJSON + ", \"error\": null }"
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"error\": \"Internal Server Error\", \"data\": null }"
            );
        }
    }

    // GET /users/:username
    public void getUserByUsername(String username) {

    }

    // POST /users
    public void createUser() {

    }

    // DELETE /users/:username
    public void deleteUser(String username) {

    }
}
