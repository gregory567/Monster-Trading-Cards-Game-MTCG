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

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }
}
