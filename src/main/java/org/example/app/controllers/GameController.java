package org.example.app.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.repositories.GameRepository;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class GameController extends Controller {

    private GameRepository gameRepository;
    private ObjectMapper objectMapper;

    public GameController(GameRepository gameRepository) {
        setGameRepository(gameRepository);
        setObjectMapper(new ObjectMapper());
    }

    public Response carryOutBattle(String username1, String username2) {
        try {
            String battleLog = getGameRepository().carryOutBattle(username1, username2);

            if (!battleLog.isEmpty()) {
                String battleLogJSON = getObjectMapper().writeValueAsString(battleLog);
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", battleLogJSON, null);
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
