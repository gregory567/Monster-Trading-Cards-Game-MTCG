package org.example.app.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.app.repositories.CardRepository;
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
            List<CardDTO> cardData = getCardRepository().getCards(username);

            if (!cardData.isEmpty()) {
                String cardDataJSON = getObjectMapper().writeValueAsString(cardData);
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", cardDataJSON, "Data successfully retrieved");
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            } else {
                return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, null);
            }

        } catch (JsonProcessingException e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Internal Server Error");
        }
    }

    // GET /deck
    public Response getDeck(String username) {
        try {
            List<CardDTO> deckData = getCardRepository().getDeck(username);

            if (!deckData.isEmpty()) {
                String deckDataJSON = getObjectMapper().writeValueAsString(deckData);
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", deckDataJSON, "Deck successfully retrieved");
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            } else {
                return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, null);
            }

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

    // PUT /deck
    public Response updateDeck(String username, String body) {
        try {
            List<String> cardIds = extractCardIdsFromBody(body);

            // Check if the provided deck includes the required amount of cards
            if (cardIds.size() != 4) {
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "The provided deck must include exactly four cards");
            }

            // Configure the deck with the provided cards
            int configurationStatus = getCardRepository().updateDeck(username, cardIds);

            switch (configurationStatus) {
                case 200:
                    return buildJsonResponse(HttpStatus.OK, null, "Deck successfully configured");
                case 403:
                    return buildJsonResponse(HttpStatus.FORBIDDEN, null, "At least one of the provided cards does not belong to the user or is not available");
                default:
                    return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to configure deck");
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to configure deck");
        }
    }


    private List<String> extractCardIdsFromBody(String body) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(body);

        List<String> cardIds = new ArrayList<>();

        for (JsonNode nodeId : jsonNode) {
            cardIds.add(nodeId.asText());
        }

        return cardIds;
    }

    // POST /packages
    public Response createPackage(String body) {
        try {
            List<CardDTO> cards = parseCardDTOsFromBody(body);

            // Check if the provided package includes the required amount of cards
            if (cards.size() != 5) {
                return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "The provided package must include exactly 5 cards");
            }

            // Configure the user's deck with the provided cards
            int configurationStatus = getCardRepository().createPackage(cards);

            switch (configurationStatus) {
                case 201:
                    return buildJsonResponse(HttpStatus.CREATED, null, "Package and cards successfully created");
                case 400:
                    return buildJsonResponse(HttpStatus.BAD_REQUEST, null, "The provided package must include exactly 5 cards");
                case 409:
                    return buildJsonResponse(HttpStatus.CONFLICT, null, "At least one card in the packages already exists");
                default:
                    return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create package and cards");
            }
        } catch (Exception e) {
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to create package and cards");
        }
    }

    // Helper method to parse CardDTO objects from the JSON body
    private List<CardDTO> parseCardDTOsFromBody(String body) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(body);

        List<CardDTO> cards = new ArrayList<>();

        for (JsonNode node : jsonNode) {
            CardDTO cardDTO = objectMapper.treeToValue(node, CardDTO.class);
            cards.add(cardDTO);
        }

        return cards;
    }

    // POST /transactions/packages
    public Response buyPackage(String username) {
        try {
            // Assuming buyPackage is a method in CardRepository to handle package purchase
            List<CardDTO> purchasedCards = getCardRepository().buyPackage(username);

            if (!purchasedCards.isEmpty()) {
                // Return a successful response with the purchased cards
                String purchasedCardsJSON = getObjectMapper().writeValueAsString(purchasedCards);
                String jsonResponse = String.format("{ \"data\": %s, \"message\": %s }", purchasedCardsJSON, "Package and cards successfully bought");
                return new Response(HttpStatus.OK, ContentType.JSON, jsonResponse);
            } else {
                // Handle scenarios where no cards are purchased
                return buildJsonResponse(HttpStatus.NOT_FOUND, null, "No card package available for buying");
            }
        } catch (CardRepository.InsufficientFundsException e) {
            // Handle insufficient funds scenario
            return buildJsonResponse(HttpStatus.FORBIDDEN, null, "Not enough money for buying a card package");
        } catch (CardRepository.CardPackageNotFoundException e) {
            // Handle scenario where no card package is available
            return buildJsonResponse(HttpStatus.NOT_FOUND, null, "No card package available for buying");
        } catch (Exception e) {
            // Handle other exceptions
            return buildJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, null, "Failed to buy package");
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
