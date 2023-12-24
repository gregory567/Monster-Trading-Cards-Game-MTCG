package org.example.app.daos;

import org.example.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.dtos.CardDTO;
import org.example.app.repositories.CardRepository;

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
        // Validate that the cardIds are unique
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
        for (CardDTO card : cards) {
            createCard(card);
        }

        // If validation passes and cards are created, proceed with creating the package
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
    public void createCard(CardDTO cardDTO) {

        // Get values from CardDTO
        UUID id = UUID.fromString(cardDTO.getId());
        String name = cardDTO.getName();
        double damage = cardDTO.getDamage();
        String elementType;

        // Determine elementType based on card name
        if (name.contains("Water")) {
            elementType = "WATER";
        } else if (name.contains("Fire")) {
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

    public List<CardDTO> buyPackage(String username) throws CardRepository.InsufficientFundsException, CardRepository.CardPackageNotFoundException {
        // Select a random package from the "Package" table
        List<CardDTO> purchasedCards = getRandomPackage();

        // Check if the purchasedCards list is empty, indicating that no package was found
        if (purchasedCards.isEmpty()) {
            throw new CardRepository.CardPackageNotFoundException("No card package available for buying");
        }

        // Check if the user has enough funds
        if (userHasSufficientFunds(username)) {

            try{
                connection.setAutoCommit(false); // Start a transaction
                // Deduct the funds from the user's account
                updateUserCoins(username, getUserCoins(username) - 5);

                // Add the purchased cards to the user's stack
                addCardsToUserStack(username, purchasedCards);

                connection.commit(); // Commit the transaction

                // Return the purchased cards
                return purchasedCards;
            } catch (SQLException e) {
                try {
                    connection.rollback(); // Rollback the transaction in case of exception
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                throw new CardRepository.InsufficientFundsException("Not enough money for buying a card package");
            } finally {
                try {
                    connection.setAutoCommit(true); // Reset auto-commit to true
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new CardRepository.InsufficientFundsException("Not enough money for buying a card package");
        }
    }

    /**
     * Selects a random package from the "Package" table.
     *
     * @return A list of CardDTO representing the cards in the selected package.
     */
    private List<CardDTO> getRandomPackage() {
        String query = "SELECT * FROM \"Package\" ORDER BY RANDOM() LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                List<CardDTO> cards = new ArrayList<>();
                for (int i = 2; i <= 6; i++) {
                    String cardId = resultSet.getObject(i, UUID.class).toString();
                    cards.add(read(cardId)); // read() method retrieves a CardDTO by cardId
                }
                return cards;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    // method to check if the user has sufficient funds
    private boolean userHasSufficientFunds(String username) throws CardRepository.InsufficientFundsException {

        double packageCost = 5;
        double userCoins = getUserCoins(username);

        if (userCoins >= packageCost) {
            return true;
        } else {
            throw new CardRepository.InsufficientFundsException("Not enough money for buying a card package");
        }
    }

    // Example method to update the user's coins
    private void updateUserCoins(String username, double updatedCoins) throws SQLException {

        String updateQuery = "UPDATE \"User\" SET coins = ? WHERE username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setDouble(1, updatedCoins);
            preparedStatement.setString(2, username);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper transaction handling
        }
    }

    /**
     * Gets the user's current coins from the database.
     *
     * @param username The username of the user.
     * @return The user's current coins.
     */
    private double getUserCoins(String username) {
        String query = "SELECT coins FROM \"User\" WHERE username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("coins");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return a default value or throw an exception based on your application requirements
        return 0.0;
    }

    // method to add purchased cards to the user's stack
    private void addCardsToUserStack(String username, List<CardDTO> purchasedCards) throws SQLException {

        String insertQuery = "INSERT INTO \"Stack\" (username, card_id) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            for (CardDTO card : purchasedCards) {
                preparedStatement.setString(1, username);
                preparedStatement.setObject(2, UUID.fromString(card.getId()));
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper transaction handling
        }

    }

    public CardDTO read(String cardId) {
        // Implement logic to retrieve a card by its ID from the database
        String query = "SELECT * FROM \"Card\" WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, java.util.UUID.fromString(cardId));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return createCardDTOFromResultSet(resultSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return null if the card with the specified ID is not found
        return null;
    }

    public void delete(String cardId) {
        // Implement logic to delete a card by its ID from the database
    }
}

