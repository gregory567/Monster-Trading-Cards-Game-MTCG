package org.example.app.daos;

import org.example.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.dtos.CardDTO;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

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

    public Integer createPackage(List<CardDTO> cards) {

        // Validate that the cards in the package are unique
        Set<String> uniqueCardIds = cards.stream().map(CardDTO::getId).collect(Collectors.toSet());
        if (uniqueCardIds.size() != 5) {
            return 400; // Return 400 for non-unique card IDs
        }

        // Validate that each card in the package is not part of any other package
        if (!areCardsNotInOtherPackages(cards)) {
            return 409; // Return 409 for cards already in another package
        }

        // create a Card entry in the Card table for each card object in the package
        for (int i = 0; i < 5; i++){
            createCard(cards.get(i));
        }

        // If validation passes, proceed with creating the package
        String insertQuery = "INSERT INTO \"Package\" (id, card1_id, card2_id, card3_id, card4_id, card5_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            // Generate a new UUID for the package
            java.util.UUID packageId = java.util.UUID.randomUUID();
            preparedStatement.setObject(1, packageId);

            // Set the card IDs for each card in the package
            for (int i = 0; i < 5; i++) {
                preparedStatement.setObject(i + 2, java.util.UUID.fromString(cards.get(i).getId()));
            }

            int rowsInserted = preparedStatement.executeUpdate();

            return rowsInserted > 0 ? 201 : 500; // Return 201 for success, 500 for other failures
        } catch (SQLException e) {
            e.printStackTrace();
            return 500; // Return 500 for SQL exceptions
        }
    }

    // method to create a new card from a CardDTO object
    private void createCard(CardDTO cardDTO) {
        // Enum to represent card names
        enum CardName {
            WaterGoblin, FireGoblin, RegularGoblin,
            WaterTroll, FireTroll, RegularTroll,
            WaterElf, FireElf, RegularElf,
            WaterSpell, FireSpell, RegularSpell,
            Knight, Dragon, Ork, Kraken
        }

        // Get values from CardDTO
        UUID id = UUID.fromString(cardDTO.getId());
        String name = cardDTO.getName();
        double damage = cardDTO.getDamage();
        String elementType;

        // Determine elementType based on card name
        if (name.equals("WaterGoblin") || name.equals("WaterTroll") || name.equals("WaterElf") || name.equals("WaterSpell") || name.equals("Kraken")) {
            elementType = "WATER";
        } else if (name.equals("FireGoblin") || name.equals("FireTroll") || name.equals("FireElf") || name.equals("FireSpell") || name.equals("Dragon")) {
            elementType = "FIRE";
        } else {
            elementType = "NORMAL";
        }

        // Set specialties array
        String[] specialties = {name};

        // Determine cardType based on card name
        String cardType;
        if (name.equals("WaterGoblin") || name.equals("FireGoblin") || name.equals("RegularGoblin") ||
                name.equals("WaterTroll") || name.equals("FireTroll") || name.equals("RegularTroll") ||
                name.equals("WaterElf") || name.equals("FireElf") || name.equals("RegularElf") ||
                name.equals("Knight") || name.equals("Dragon") || name.equals("Ork") || name.equals("Kraken")) {
            cardType = "MONSTER";
        } else {
            cardType = "SPELL";
        }

        // SQL statement to insert a new card into the Card table
        String insertStmt = "INSERT INTO \"Card\" (id, name, damage, elementType, specialties, cardType) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(insertStmt)) {
            // Set parameters in the prepared statement
            preparedStatement.setObject(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setDouble(3, damage);
            preparedStatement.setString(4, elementType);
            preparedStatement.setArray(5, getConnection().createArrayOf("VARCHAR", specialties));
            preparedStatement.setString(6, cardType);

            // Execute the SQL update statement to insert the new card
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            // Print any SQL exception that occurs during card creation
            e.printStackTrace();
        }
    }

    /**
     * Checks if the provided cards are not part of any other package.
     *
     * @param cards The list of cards to be validated.
     * @return True if the cards are not part of any other package, false otherwise.
     */
    private boolean areCardsNotInOtherPackages(List<CardDTO> cards) {
        String query = "SELECT COUNT(*) FROM \"Package\" WHERE card1_id = ? OR card2_id = ? OR card3_id = ? OR card4_id = ? OR card5_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (CardDTO card : cards) {
                preparedStatement.setObject(1, java.util.UUID.fromString(card.getId()));

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    if (count > 0) {
                        return false; // At least one card is part of another package
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
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

