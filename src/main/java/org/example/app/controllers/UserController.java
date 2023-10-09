package org.example.app.controllers;

import org.example.app.services.UserService;
import org.fasterxml.jackson.core.JsonProcessingException;
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

    // DELETE /users/:id -> löscht einen user mit der id
    // POST /users -> erstellt einen neuen user
    // PUT/PATCH /users/:id -> updated einen user mit der id
    // GET /users/:id -> gibt einen user zurück mit der id
    // GET /cities -> gibt alle users zurück
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

    // GET /users/:id
    public void getUserById(int id) {

    }

    // POST /users
    public void createUser() {

    }

    // DELETE /users/:id
    public void deleteUser(int id) {

    }
}
