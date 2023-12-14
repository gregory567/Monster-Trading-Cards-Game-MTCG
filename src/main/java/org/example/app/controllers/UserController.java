package org.example.app.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private ObjectMapper objectMapper;

    public UserController(UserRepository userRepository) {
        setUserRepository(userRepository);
        setObjectMapper(new ObjectMapper());  // Initialize objectMapper
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
    public Response getUser(String username) {
        try {
            // Retrieve the user data based on the username from the UserService
            User user = getUserRepository().get(username);

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
    public Response createUser(String body) {
        try {
            String username = extractUsernameFromBody(body);
            String password = extractPasswordFromBody(body);

            // Attempt to add the user and check the result
            int result = getUserRepository().add(username, password);

            if (result == 1) {
                // User created successfully
                return buildJsonResponse(HttpStatus.CREATED, null, "User successfully created");
            } else if (result == 0) {
                // User creation failed due to duplicate username
                return buildJsonResponse(HttpStatus.CONFLICT, null, "User with the same username already registered");
            } else {
                // Handle other scenarios as needed
                return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create user");
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create user");
        }
    }

    public Response updateUser(String username, String body) {
        try {
            String token = extractTokenFromBody(body);
            String name = extractNameFromBody(body);
            String bio = extractBioFromBody(body);
            String image = extractImageFromBody(body);

            int updateStatus = getUserRepository().updateUser(username, token, name, bio, image);

            switch (updateStatus) {
                case 200:
                    return buildJsonResponse(HttpStatus.OK, null, "User successfully updated");
                case 401:
                    return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
                case 404:
                    return buildJsonResponse(HttpStatus.NOT_FOUND, null, "User not found");
                default:
                    return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to update user");
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to update user");
        }
    }

    // DELETE /users/:username
    public Response deleteUser(String username) {
        try {
            getUserRepository().remove(username);
            return buildJsonResponse(HttpStatus.NO_CONTENT, null, null);
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to delete user");
        }
    }

    public Response loginUser(String body) {
        // Implement the logic for user login based on the request body
        // Modify the logic based on your actual authentication mechanism
        return null;
    }

    private String extractUsernameFromBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.get("Username").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractPasswordFromBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.get("Password").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractTokenFromBody(String body) {
        //implement
        return null;
    }

    private String extractNameFromBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.get("Name").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractBioFromBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.get("Bio").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractImageFromBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.get("Image").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }

}
