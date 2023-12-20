package org.example.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.app.repositories.CardRepository;
import org.example.http.ContentType;
import org.example.http.HttpStatus;
import org.example.server.Response;
import org.example.app.dtos.CardDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CardController extends Controller {

    private CardRepository cardRepository;
    private ObjectMapper objectMapper;

    public CardController(CardRepository cardRepository) {
        setCardRepository(cardRepository);
        setObjectMapper(new ObjectMapper());
    }

    // GET /cards
    public Response getCards(String username) {
        try {


            // Assuming readAll is a method in CardRepository to get all cards
            List<CardDTO> cardData = getCardRepository().getAll(username);
            String cardDataJSON = getObjectMapper().writeValueAsString(cardData);

            String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", cardDataJSON, "Data successfully retrieved");
            return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);

        } catch (JsonProcessingException e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        }
    }

    // GET /cards/:cardId
    public Response getCard(String cardId) {
        try {
            // Assuming read is a method in CardRepository to get a specific card by ID
            CardDTO cardDTO = getCardRepository().get(cardId);

            // Check if the cardDTO is found
            if (cardDTO != null) {
                // Convert the cardDTO object to JSON
                String cardDataJSON = getObjectMapper().writeValueAsString(cardDTO);
                // Return a successful response with the card data and additional message
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", cardDataJSON, "Data successfully retrieved");
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            } else {
                // Return a not found response if the card is not found
                return buildJsonResponse(HttpStatus.NOT_FOUND, null, "Card not found");
            }
        } catch (JsonProcessingException e) {
            // Handle JSON processing exception
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        } catch (Exception e) {
            // Handle other exceptions
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to retrieve card");
        }
    }

    // POST /cards
    public Response createCard(String body) {
        try {
            // Assuming create is a method in CardRepository to add a new card
            CardDTO cardDTO = parseCardDTOFromBody(body);

            // Attempt to add the card and check the result
            int result = getCardRepository().add(cardDTO);

            if (result == 201) {
                // Card created successfully
                return buildJsonResponse(HttpStatus.CREATED, null, "Card successfully created");
            } else {
                // Handle other scenarios as needed
                return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create card");
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create card");
        }
    }

    // PUT /cards/:cardId
    public Response updateCard(String cardId, String body) {
        try {
            // Assuming update is a method in CardRepository to update a card
            CardDTO cardDTO = parseCardDTOFromBody(body);
            cardDTO.setId(cardId); // Set the ID from the path parameter

            int updateStatus = getCardRepository().update(cardDTO);

            switch (updateStatus) {
                case 200:
                    return buildJsonResponse(HttpStatus.OK, null, "Card successfully updated");
                case 404:
                    return buildJsonResponse(HttpStatus.NOT_FOUND, null, "Card not found");
                default:
                    return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to update card");
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to update card");
        }
    }

    // DELETE /cards/:cardId
    public Response deleteCard(String cardId) {
        try {
            // Assuming delete is a method in CardRepository to delete a card by ID
            getCardRepository().remove(cardId);
            return buildJsonResponse(HttpStatus.NO_CONTENT, null, null);
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to delete card");
        }
    }

    private CardDTO parseCardDTOFromBody(String body) throws JsonProcessingException {
        return getObjectMapper().readValue(body, CardDTO.class);
    }

    private Response buildJsonResponse(HttpStatus status, String data, String error) {
        String jsonResponse = String.format("{ \"data\": %s, \"error\": %s }", data, error);
        return new Response(status, ContentType.JSON, jsonResponse);
    }
}
