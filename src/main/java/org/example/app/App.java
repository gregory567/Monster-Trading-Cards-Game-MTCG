package org.example.app;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class App implements ServerApp {

    private UserController userController;
    private CardController cardController;
    private TradeDealController tradeDealController;
    private GameController gameController;
    private String authenticatedUserToken;
    private boolean isBattleLobbyOpen = false;
    private List<String> usernamesInLobby = new ArrayList<>();

    public synchronized void notifyNewUserInLobby() {
        isBattleLobbyOpen = true;
        // Notify any waiting threads that the lobby is now open
        notifyAll();
    }

    public synchronized boolean isBattleLobbyOpen() {
        return isBattleLobbyOpen;
    }

    public synchronized void addUsernameToLobby(String username) {
        usernamesInLobby.add(username);
    }

    public synchronized List<String> getUsernamesInLobby() {
        return new ArrayList<>(usernamesInLobby);
    }

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
            String userToken = request.getUserToken();

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
            String userToken = request.getUserToken();

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
            String userToken = request.getUserToken();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            return getCardController().getDeck(usernameFromToken);
        } else if (request.getPathname().equals("/stats")) {
            // Extract the user token from the request
            String userToken = request.getUserToken();

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
            String userToken = request.getUserToken();

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
            String userToken = request.getUserToken();

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
                return loginResponse;
            } else {
                return loginResponse;
            }
        } else if (request.getPathname().equals("/packages")) {

            // Extract the user token from the request
            String userToken = request.getUserToken();

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
            String userToken = request.getUserToken();

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
            String userToken = request.getUserToken();

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
            String userToken = request.getUserToken();

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
            String userToken = request.getUserToken();

            // Check if the user token is null or empty
            if (userToken == null || userToken.isEmpty()) {
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Get the user from the token
            String usernameFromToken = getAuthenticatedUsernameFromToken(userToken);
            if (!authenticateUser(request, usernameFromToken)) { // authentication check
                return buildJsonResponse(HttpStatus.UNAUTHORIZED, null, "Access token is missing or invalid");
            }

            // Notify the waiting threads that a new user is in the lobby
            notifyNewUserInLobby();
            /*
            In this case, when a user requests to enter the battles endpoint,
            the server notifies waiting threads that a new user is in the lobby.
            This is important for signaling other threads waiting for a user to enter the lobby and potentially start a battle.
             */

            // If there is another user in the lobby, start the battle immediately
            List<String> usernamesInLobby = getUsernamesInLobby();
            if (isBattleLobbyOpen() && usernamesInLobby.size() == 2) {
                return getGameController().carryOutBattle(usernamesInLobby.get(0), usernamesInLobby.get(1));
            } else {
                // Otherwise, return a response indicating that the user is in the lobby
                return buildJsonResponse(HttpStatus.OK, null, "Waiting for another user to enter the lobby");
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
            String userToken = request.getUserToken();

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
            String userToken = request.getUserToken();

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
        String userToken = request.getUserToken();

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

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }

    private void handleException(Exception e) {
        e.printStackTrace();
    }

}
