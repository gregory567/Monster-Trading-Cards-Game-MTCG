package org.example.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;
import org.example.app.controllers.CardController;
import org.example.app.controllers.TradeDealController;
import org.example.app.controllers.UserController;
import org.example.app.controllers.GameController;
import org.example.app.daos.CardDAO;
import org.example.app.daos.TradeDealDAO;
import org.example.app.daos.UserDAO;
import org.example.app.daos.GameDAO;
import org.example.app.repositories.CardRepository;
import org.example.app.repositories.TradeDealRepository;
import org.example.app.repositories.UserRepository;
import org.example.app.repositories.GameRepository;
import org.example.app.services.DatabaseService;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Request;
import org.example.server.Response;
import org.example.server.ServerApp;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class App implements ServerApp {

    private UserController userController;
    private CardController cardController;
    private TradeDealController tradeDealController;
    private GameController gameController;
    private final ReentrantLock lock = new ReentrantLock();
    private String userName1;
    private String userName2;
    private String battleLog;


    public App() {
        DatabaseService databaseService = new DatabaseService();
        UserDAO userDAO = new UserDAO(databaseService.getConnection());
        CardDAO cardDAO = new CardDAO(databaseService.getConnection());
        TradeDealDAO tradeDealDAO = new TradeDealDAO(databaseService.getConnection());
        GameDAO gameDAO = new GameDAO(databaseService.getConnection());

        UserRepository userRepository = new UserRepository(userDAO);
        CardRepository cardRepository = new CardRepository(cardDAO);
        TradeDealRepository tradeDealRepository = new TradeDealRepository(tradeDealDAO);
        GameRepository gameRepository = new GameRepository(gameDAO);

        setUserController(new UserController(userRepository));
        setCardController(new CardController(cardRepository));
        setTradeDealController(new TradeDealController(tradeDealRepository));
        setGameController(new GameController(gameRepository));
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
            handleException(e);
        } catch (Exception e) {
            handleException(e);
            return internalServerErrorResponse();
        }
        return notFoundResponse();
    }

    private Response handleGetRequest(Request request) throws InterruptedException {
        if (request.getPathname().equals("/users")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Check if the user associated with the token has the necessary permissions
            String authenticatedUsername = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticatedUsername.equals("admin")) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            testMultithreading();
            return getUserController().getUsers();
        } else if (request.getPathname().startsWith("/users/")) {
            String usernameFromPath = getUsernameFromPath(request.getPathname());
            if (!authenticateUser(request, usernameFromPath)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }
            return getUserController().getUser(usernameFromPath);
        } else if (request.getPathname().equals("/cards")) {

            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            return getCardController().getCards(usernameFromToken);
        } else if (request.getPathname().equals("/deck")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Extract the format parameter from the query parameters
            String format = getFormatParameter(request.getParams());

            // Pass the format parameter to the getDeck method
            return getCardController().getDeck(usernameFromToken, format);
        } else if (request.getPathname().equals("/stats")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            return getUserController().getStats(usernameFromToken);
        } else if (request.getPathname().equals("/scoreboard")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            return getUserController().getScoreBoard();
        } else if (request.getPathname().equals("/tradings")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            return getTradeDealController().getTradeDeals();
        }
        return notFoundResponse();
    }

    private Response handlePostRequest(Request request) {
        if (request.getPathname().equals("/users")) {
            String body = request.getBody();
            return getUserController().createUser(body);
        } else if (request.getPathname().equals("/sessions")) {
            String body = request.getBody();
            return getUserController().loginUser(body);
        } else if (request.getPathname().equals("/packages")) {

            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!usernameFromToken.equals("admin")) {
                return buildJsonResponse(HttpStatus.FORBIDDEN, null, "Provided user is not \"admin\"");
            }

            String body = request.getBody();
            return getCardController().createPackage(body);
        } else if (request.getPathname().equals("/transactions/packages")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            return getCardController().buyPackage(usernameFromToken);
        } else if (request.getPathname().equals("/tradings")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            String body = request.getBody();
            return getTradeDealController().createTrade(usernameFromToken, body);
        } else if (request.getPathname().startsWith("/tradings/")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            String body = request.getBody();
            String tradeDealId = getTradeDealIdFromPath(request.getPathname());
            return getTradeDealController().carryOutTrade(usernameFromToken, tradeDealId, body);
        } else if (request.getPathname().equals("/battles")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            synchronized (lock) {
                // Wait until there are two users in the lobby
                if (userName1 == null) {
                    // add first username to usernamesInLobby
                    userName1 = usernameFromToken;
                    System.out.println("Waiting for another user to enter the lobby");
                } else if (userName2 == null) {
                    // If there are two users in the lobby, start the battle
                    // add second username to usernamesInLobby
                    userName2 = usernameFromToken;
                    System.out.println("Both usernames are set in the lobby");

                    // Call the GameController to carry out the battle
                    // carry out the battle, return the battle log
                    Response battleResponse = getGameController().carryOutBattle(userName1, userName2);
                    setBattleLog(extractBattleLog(battleResponse));

                    return battleResponse;
                } else {
                    // Otherwise, print a message to the console indicating that the battle already started
                    System.out.println("Battle already started");
                }
            }
        }
        return notFoundResponse();
    }

    private Response handlePutRequest(Request request) {
        if (request.getPathname().startsWith("/users/")) {
            String usernameFromPath = getUsernameFromPath(request.getPathname());
            if (!authenticateUser(request, usernameFromPath)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }
            String body = request.getBody();
            return getUserController().updateUser(usernameFromPath, body);
        } else if (request.getPathname().equals("/deck")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            String body = request.getBody();
            return getCardController().updateDeck(usernameFromToken, body);
        }
        return notFoundResponse();
    }

    private Response handleDeleteRequest(Request request) {
        if (request.getPathname().startsWith("/users/")) {
            String usernameFromPath = getUsernameFromPath(request.getPathname());
            if (!authenticateUser(request, usernameFromPath)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }
            return getUserController().deleteUser(usernameFromPath);
        } else if (request.getPathname().startsWith("/tradings/")) {
            // Extract the user token from the request
            String userToken = request.getAuthorization();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            String tradeDealIdFromPath = getTradeDealIdFromPath(request.getPathname());
            return getTradeDealController().deleteTradeDeal(usernameFromToken, tradeDealIdFromPath);
        }
        return notFoundResponse();
    }

    private String getUsernameFromPath(String path) {
        // Extract username from path, e.g., "/users/john" -> "john"
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    private String getTradeDealIdFromPath(String path) {
        // Extract trade deal ID from path, e.g., "/tradings/3fa85f64-5717-4562-b3fc-2c963f66afa6" -> "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    // Method to extract the format parameter from the query parameters
    private String getFormatParameter(String params) {
        // Check if the params string is not null and not empty
        if (params != null && !params.isEmpty()) {
            // Split the params string into individual parameter pairs using "&" as the delimiter
            String[] paramPairs = params.split("&");

            // Iterate through each parameter pair
            for (String pair : paramPairs) {
                // Split the parameter pair into key and value using "=" as the delimiter
                String[] keyValue = pair.split("=");

                // Check if the key-value pair has exactly two elements and the key is "format"
                if (keyValue.length == 2 && keyValue[0].equals("format")) {
                    // Return the value associated with the "format" key
                    return keyValue[1];
                }
            }
        }
        // If no "format" parameter is found, return json
        return "json";
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

    private boolean authenticateUser(Request request, String username) {
        // Extract the user token from the request
        String userToken = request.getAuthorization();

        // Check if the user token is null or empty
        if (userToken == null || userToken.isEmpty()) {
            return false;
        }

        // Check if the user associated with the token has the necessary permissions
        String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
        if (!usernameFromToken.equals("admin") && !usernameFromToken.equals(username)) {
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

    private String extractBattleLog(Response response) {
        if (response == null || response.getContent() == null || response.getContent().isEmpty()) {
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getContent());

            if (rootNode.has("data")) {
                JsonNode dataNode = rootNode.get("data");
                if (dataNode != null && dataNode.isTextual()) {
                    return dataNode.asText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }

    private void handleException(Exception e) {
        e.printStackTrace();
    }

}
