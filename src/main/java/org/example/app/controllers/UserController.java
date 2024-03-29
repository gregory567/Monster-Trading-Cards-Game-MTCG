package org.example.app.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.app.repositories.UserRepository;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;
import org.example.app.dtos.UserDataDTO;
import org.example.app.dtos.UserStatDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class UserController extends Controller {

    private UserRepository userRepository;
    private ObjectMapper objectMapper;

    public UserController(UserRepository userRepository) {
        setUserRepository(userRepository);
        setObjectMapper(new ObjectMapper());
    }

    // GET /users
    public Response getUsers() {
        try {
            List<UserDataDTO> userData = getUserRepository().getUsers();
            ArrayNode userArray = getObjectMapper().createArrayNode();

            if (userData != null) {
                for (UserDataDTO user : userData) {
                    // Create a JSON object for each user
                    ObjectNode userNode = getObjectMapper().createObjectNode()
                            .put("Name", user.getName())
                            .put("Bio", user.getBio())
                            .put("Image", user.getImage());
                    userArray.add(userNode);
                }

                // Convert the userArray to a string
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", userArray.toString(), "Data successfully retrieved");
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            } else {
                return buildJsonResponse(HttpStatus.NOT_FOUND, null, "Users not found");
            }
        } catch (Exception e) {
            // Handle JSON processing exception
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        }
    }

    // GET /users/:username
    public Response getUser(String username) {
        try {
            // Retrieve the user data based on the username from the UserRepository
            UserDataDTO userdata = getUserRepository().getUser(username);

            // Check if the userdata is found
            if (userdata != null) {
                // Create a JSON object for the user
                ObjectNode userNode = getObjectMapper().createObjectNode()
                        .put("Name", userdata.getName())
                        .put("Bio", userdata.getBio())
                        .put("Image", userdata.getImage());

                // Convert the userNode to a string
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", userNode.toString(), "Data successfully retrieved");
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            } else {
                // Return a not found response if the user is not found
                return buildJsonResponse(HttpStatus.NOT_FOUND, null, "User not found");
            }
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
            int result = getUserRepository().createUser(username, password);

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
            getUserRepository().deleteUser(username);
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
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "Missing username or password");
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
                case "500":
                    return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to authenticate user");
                default:
                    // Authentication successful
                    // Include the token in the JSON response
                    String jsonResponse = String.format("{ \"data\": { \"token\": \"%s\" }, \"error\": null }", authenticationStatus);
                    return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to authenticate user");
        }
    }

    public Response logoutUser(String username) {
        try {
            // Validate username and password
            if (username == null) {
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "Missing username");
            }

            // Authenticate the user using the UserDAO
            String logoutStatus = getUserRepository().logoutUser(username);

            switch (logoutStatus) {
                case "404":
                    // User not found
                    return buildJsonResponse(HttpStatus.NOT_FOUND, null, "User not found");
                case "500":
                    return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to logout user");
                default:
                    // Assuming the logout operation was successful, return a response with HTTP status 200 (OK)
                    return buildJsonResponse(HttpStatus.OK, null, "User successfully logged out");
            }
        } catch (Exception e) {
            // Handle exceptions, if any
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to logout user");
        }
    }

    // GET /stats
    public Response getStats(String username) {
        try {
            // Retrieve user statistics from the UserRepository for the specified username
            UserStatDTO userStat = getUserRepository().getStats(username);

            if (userStat != null) {
                // Create a JSON object representing the user statistics
                JsonNode userStatNode = getObjectMapper().createObjectNode()
                        .put("Name", userStat.getName())
                        .put("Elo", userStat.getElo_score())
                        .put("Wins", userStat.getWins())
                        .put("Losses", userStat.getLosses());

                // Convert the JSON object to a string
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", userStatNode.toString(), "User statistics successfully retrieved");

                // Return a successful response with user statistics
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            } else {
                // Return a not found response if user statistics are not available
                return buildJsonResponse(HttpStatus.NOT_FOUND, null, "User statistics not found");
            }

        } catch (Exception e) {
            // Handle exceptions
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to retrieve user statistics");
        }
    }

    // GET /scoreboard
    public Response getScoreBoard() {
        try {
            // Retrieve the scoreboard data from the UserRepository
            List<UserStatDTO> scoreboard = getUserRepository().getScoreBoard();

            ArrayNode scoreboardArray = getObjectMapper().createArrayNode();

            // Create a JSON object for each user in the scoreboard
            for (UserStatDTO userStat : scoreboard) {
                ObjectNode userStatNode = getObjectMapper().createObjectNode()
                        .put("Name", userStat.getName())
                        .put("Elo", userStat.getElo_score())
                        .put("Wins", userStat.getWins())
                        .put("Losses", userStat.getLosses());
                scoreboardArray.add(userStatNode);
            }

            // Convert the scoreboardArray to a string
            String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", scoreboardArray.toString(), "Scoreboard successfully retrieved");
            return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
        } catch (Exception e) {
            // Handle exceptions
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to retrieve scoreboard");
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
