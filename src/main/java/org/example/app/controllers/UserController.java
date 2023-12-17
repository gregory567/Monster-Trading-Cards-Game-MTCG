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
import org.example.app.models.Userdata;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class UserController extends Controller {

    private UserRepository userRepository;
    private ObjectMapper objectMapper;
    private String authenticatedUserToken;

    public UserController(UserRepository userRepository) {
        setUserRepository(userRepository);
        setObjectMapper(new ObjectMapper());
    }

    // DELETE /users/:username -> löscht einen user mit dem usernamen
    // POST /users -> erstellt einen neuen user
    // PUT/PATCH /users/:username -> updated einen user mit dem usernamen
    // GET /users/:username -> gibt einen user zurück mit dem usernamen
    // GET /users -> gibt alle users zurück
    public Response getUsers() {
        try {
            List<Userdata> userData = getUserRepository().getAll();
            String userDataJSON = getObjectMapper().writeValueAsString(userData);

            String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", userDataJSON, "Data successfully retrieved");
            return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);

        } catch (JsonProcessingException e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        }
    }

    // GET /users/:username
    public Response getUser(String username) {
        try {
            // Retrieve the user data based on the username from the UserRepository
            Userdata userdata = getUserRepository().get(username);

            // Check if the userdata is found
            if (userdata != null) {
                // Convert the userdata object to JSON
                String userDataJSON = getObjectMapper().writeValueAsString(userdata);
                // Return a successful response with the user data and additional message
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", userDataJSON, "Data successfully retrieved");
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
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
            if (!isValidUserRequestBody(body)) {
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "Invalid user request body");
            }

            String username = extractUsernameFromBody(body);
            String password = extractPasswordFromBody(body);

            // Attempt to add the user and check the result
            int result = getUserRepository().add(username, password);

            if (result == 201) {
                // User created successfully
                return buildJsonResponse(HttpStatus.CREATED, null, "User successfully created");
            } else if (result == 409) {
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
            if (!isValidUserUpdateRequestBody(body)) {
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "Invalid user update request body");
            }

            String name = extractNameFromBody(body);
            String bio = extractBioFromBody(body);
            String image = extractImageFromBody(body);

            int updateStatus = getUserRepository().updateUser(username, name, bio, image);

            switch (updateStatus) {
                case 200:
                    return buildJsonResponse(HttpStatus.OK, null, "User successfully updated");
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
        try {
            if (!isValidLoginRequestBody(body)) {
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "Invalid login request body");
            }

            String username = extractUsernameFromBody(body);
            String password = extractPasswordFromBody(body);

            // Validate username and password
            if (username == null || password == null) {
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "Invalid username or password");
            }

            // Authenticate the user using the UserDAO
            String authenticationStatus = getUserRepository().loginUser(username, password);

            switch (authenticationStatus) {
                case "401":
                    // Incorrect username or password
                    return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Invalid username or password provided");
                case "404":
                    // User not found
                    return buildJsonResponse(HttpStatus.NOT_FOUND, null, "User not found");
                default:
                    // Authentication successful
                    setAuthenticatedUserToken(authenticationStatus);  // Save the user's token
                    // Include the token in the JSON response
                    String jsonResponse = String.format("{ \"data\": { \"token\": \"%s\" }, \"error\": null }", authenticationStatus);
                    return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to authenticate user");
        }
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

    // Validate the request body for the createUser method
    private boolean isValidUserRequestBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.has("Username") && jsonNode.has("Password");
        } catch (JsonProcessingException e) {
            return false; // JSON parsing error
        }
    }

    // Validate the request body for the updateUser method
    private boolean isValidUserUpdateRequestBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            // Customize this logic based on the expected fields for user updates
            return jsonNode.has("Name") || jsonNode.has("Bio") || jsonNode.has("Image");
        } catch (JsonProcessingException e) {
            return false; // JSON parsing error
        }
    }

    // Validate the request body for the loginUser method
    private boolean isValidLoginRequestBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.has("Username") && jsonNode.has("Password");
        } catch (JsonProcessingException e) {
            return false; // JSON parsing error
        }
    }

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }

}
