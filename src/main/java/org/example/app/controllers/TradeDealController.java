package org.example.app.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.app.dtos.UserStatDTO;
import org.example.app.repositories.TradeDealRepository;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;
import org.example.app.dtos.TradeDealDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class TradeDealController extends Controller {

    private TradeDealRepository tradeDealRepository;
    private ObjectMapper objectMapper;

    public TradeDealController(TradeDealRepository tradeDealRepository) {
        setTradeDealRepository(tradeDealRepository);
        setObjectMapper(new ObjectMapper());
    }

    public Response getTradeDeals() {
        try {
            List<TradeDealDTO> tradeDeals = getTradeDealRepository().getTradeDeals();

            if (tradeDeals.isEmpty()) {
                return buildJsonResponse(HttpStatus.NO_CONTENT, null, "The request was fine, but there are no trading deals available");
            }

            ArrayNode tradeDealsArray = getObjectMapper().createArrayNode();

            // Create a JSON object for each trade deal in the available trade deals array
            for (TradeDealDTO tradeDeal : tradeDeals) {
                ObjectNode tradeDealNode = getObjectMapper().createObjectNode()
                        .put("Id", tradeDeal.getId())
                        .put("CardToTrade", tradeDeal.getCardToTrade())
                        .put("Type", tradeDeal.getCardType())
                        .put("MinimumDamage", tradeDeal.getMinimumDamage());
                tradeDealsArray.add(tradeDealNode);
            }

            // Convert the tradeDealsArray to a string
            String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", tradeDealsArray.toString(), "Trade Deals successfully retrieved");
            return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to retrieve Trade Deals");
        }
    }

    public Response createTrade(String username, String body) {
        try {

            /*
            if (!isValidTradeDealRequestBody(body)) {
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "Invalid user update request body");
            }

            String Id = extractIdFromBody(body);
            String CardToTrade = extractCardToTradeFromBody(body);
            String Type = extractTypeFromBody(body);
            Double MinimumDamage = extractMinimumDamageFromBody(body);
             */

            // Deserialize the request body into TradingDealDTO
            TradeDealDTO tradeDealDTO = getObjectMapper().readValue(body, TradeDealDTO.class);

            // Ensure the trading deal is valid
            if (!isValidTradeDeal(tradeDealDTO)) {
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "Invalid trading deal request body");
            }

            // Create the trade deal
            Integer statusCode = getTradeDealRepository().createTradeDeal(username, tradeDealDTO);

            switch (statusCode) {
                case 201:
                    return buildJsonResponse(HttpStatus.CREATED, null, "Trading deal successfully created");
                case 403:
                    return buildJsonResponse(HttpStatus.FORBIDDEN, null, "The deal contains a card that is not owned by the user or locked in the deck");
                case 409:
                    return buildJsonResponse(HttpStatus.CONFLICT, null, "A deal with this deal ID already exists");
                default:
                    return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create trading deal");
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create trading deal");
        }
    }

    public Response deleteTradeDeal(String username, String tradeDealId) {
        try {

            // Delete the trade deal
            int statusCode = getTradeDealRepository().deleteTradeDeal(username, tradeDealId);

            switch (statusCode) {
                case 200:
                    return buildJsonResponse(HttpStatus.NO_CONTENT, null, "Trade deal successfully deleted");
                case 403:
                    return buildJsonResponse(HttpStatus.FORBIDDEN, null, "The deal contains a card that is not owned by the user");
                case 404:
                    return buildJsonResponse(HttpStatus.NOT_FOUND, null, "The provided deal ID was not found.");
                default:
                    return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to delete trade deal");
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to delete trade deal");
        }
    }

    private String extractIdFromBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.get("Id").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractCardToTradeFromBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.get("CardToTrade").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractTypeFromBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.get("Type").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Double extractMinimumDamageFromBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.get("MinimumDamage").asDouble();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Validate the request body for the createTrade method
    private boolean isValidTradeDealRequestBody(String body) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(body);
            return jsonNode.has("Id") && jsonNode.has("CardToTrade") && jsonNode.has("Type") && jsonNode.has("MinimumDamage");
        } catch (JsonProcessingException e) {
            return false; // JSON parsing error
        }
    }

    // Validate the request body for the createTrade method
    private boolean isValidTradeDeal(TradeDealDTO tradeDealDTO) {
        return tradeDealDTO != null && tradeDealDTO.getId() != null && tradeDealDTO.getCardToTrade() != null
                && tradeDealDTO.getCardType() != null && tradeDealDTO.getMinimumDamage() != null;
    }

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }
}
