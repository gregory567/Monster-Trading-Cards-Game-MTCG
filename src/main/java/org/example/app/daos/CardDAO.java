package org.example.app.daos;

import org.example.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.dtos.CardDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class CardDAO {

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    @Setter(AccessLevel.PRIVATE)
    ArrayList<CardDTO> cardsCache;

    public CardDAO(Connection connection) {
        setConnection(connection);
    }

    public Integer create(CardDTO cardDTO) {
        // Implement card creation logic here
        // Return appropriate HTTP status codes based on the result
        return null;
    }

    public ArrayList<CardDTO> getUserCards(String username) {
        List<CardDTO> cards = new ArrayList<>();

        String query = "SELECT * FROM \"Stack\" s JOIN \"Card\" c ON s.card_id = c.id WHERE s.username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username); // Set the parameter for the username
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CardDTO cardDTO = createCardDTOFromResultSet(resultSet);
                cards.add(cardDTO);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (ArrayList<CardDTO>) cards;
    }

    // Helper method to create a DTO from a ResultSet
    private CardDTO createCardDTOFromResultSet(ResultSet resultSet) throws SQLException {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(resultSet.getString("id"));
        cardDTO.setName(resultSet.getString("name"));
        cardDTO.setDamage(resultSet.getInt("damage"));

        return cardDTO;
    }

    public List<CardDTO> getDeckCards(String username) {
        String query = "SELECT c.* FROM \"Deck\" d " +
                "JOIN \"Card\" c ON c.id = ANY(ARRAY[d.card1_id, d.card2_id, d.card3_id, d.card4_id]) " +
                "WHERE d.username = ?";

        List<CardDTO> cards = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username); // Set the parameter for the username
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CardDTO cardDTO = createCardDTOFromResultSet(resultSet);
                cards.add(cardDTO);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cards;
    }

    /**
     * Updates the user's deck with the provided card IDs.
     *
     * @param username The username of the user whose deck is being updated.
     * @param cardIds  The list of card IDs to be added to the user's deck.
     * @return An integer status code:
     * - 200 for success
     * - 403 if at least one of the provided cards does not belong to the user or is not available
     * - 500 for other failures
     */
    public Integer updateUserDeck(String username, List<String> cardIds) {
        // Validate that cardIds are unique
        Set<String> uniqueCardIds = new HashSet<>(cardIds);
        if (uniqueCardIds.size() != 4) {
            return 400; // Return 400 for non-unique card IDs
        }

        // Validate that the provided cards belong to the user and are available
        if (!areCardsValid(username, cardIds)) {
            return 403; // Return 403 for invalid cards
        }

        // If validation passes, proceed with the update
        String updateQuery = "UPDATE \"Deck\" SET card1_id = ?, card2_id = ?, card3_id = ?, card4_id = ? WHERE username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            // cardIds is a list of UUIDs in the same order as they should be updated in the deck
            for (int i = 0; i < cardIds.size(); i++) {
                preparedStatement.setObject(i + 1, java.util.UUID.fromString(cardIds.get(i)));
            }
            preparedStatement.setString(5, username); // Set the username parameter

            int rowsUpdated = preparedStatement.executeUpdate();

            return rowsUpdated > 0 ? 200 : 500; // Return 200 for success, 500 for other failures
        } catch (SQLException e) {
            e.printStackTrace();
            return 500; // Return 500 for SQL exceptions
        }
    }

    /**
     * Checks if the provided cards are valid for updating the user's deck.
     *
     * @param username The username of the user.
     * @param cardIds  The list of card IDs to be validated.
     * @return True if the cards are valid, false otherwise.
     */
    private boolean areCardsValid(String username, List<String> cardIds) {
        // Get the user's stack cards
        List<CardDTO> userStackCards = getUserCards(username);

        // Check if each provided card is in the user's stack and not in another user's deck
        for (String cardId : cardIds) {
            boolean isCardInUserStack = userStackCards.stream().anyMatch(card -> card.getId().equals(cardId));

            // If the card is not in the user's stack or is part of another user's deck, return false
            if (!isCardInUserStack || isCardInAnotherDeck(cardId, username)) {
                return false;
            }
        }

        // If all checks pass, return true
        return true;
    }

    /**
     * Checks if a card is present in another user's deck.
     *
     * @param cardId   The ID of the card to be checked.
     * @param username The username of the user whose deck is being checked.
     * @return True if the card is in another user's deck, false otherwise.
     */
    private boolean isCardInAnotherDeck(String cardId, String username) {
        String query = "SELECT COUNT(*) FROM \"Deck\" WHERE (card1_id = ? OR card2_id = ? OR card3_id = ? OR card4_id = ?) AND username != ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 1; i <= 4; i++) {
                preparedStatement.setObject(i, java.util.UUID.fromString(cardId));
            }
            preparedStatement.setString(5, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public CardDTO read(String cardId) {
        // Implement logic to retrieve a card by its ID from the database
        // and return the Card object
        return null;
    }

    public void delete(String cardId) {
        // Implement logic to delete a card by its ID from the database
    }

    public Integer configureDeck(String username, List<String> cardIds) {
        // Implement logic to configure the user's deck with the provided cards
        // Return appropriate HTTP status codes based on the result
        return null;
    }
}

