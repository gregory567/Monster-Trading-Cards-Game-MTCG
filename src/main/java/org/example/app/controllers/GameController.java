package org.example.app.controllers;

import org.example.Battle;
import org.example.app.models.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.repositories.GameRepository;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;
import org.example.app.dtos.CardDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class GameController extends Controller {
    private List<User> users;
    private List<Battle> battles;

    private GameRepository gameRepository;
    private ObjectMapper objectMapper;

    public GameController(GameRepository gameRepository) {
        setGameRepository(gameRepository);
        setObjectMapper(new ObjectMapper());
    }

    public Response carryOutBattle(String username) {
        try {
            String battleLog = getGameRepository().carryOutBattle(username);

            if (!battleLog.isEmpty()) {
                String battleLogJSON = getObjectMapper().writeValueAsString(battleLog);
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", battleLogJSON, "The battle has been carried out successfully.");
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            } else {
                return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, null);
            }

        } catch (JsonProcessingException e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        }
    }

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }
}
