package org.example.app.daos;

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

    public CardDAO(Connection connection) {
        setConnection(connection);
    }

    /**
     * Retrieves a list of cards in the user's stack.
     *
     * @param username The username of the user.
     * @return An ArrayList of CardDTO representing the cards in the user's stack.
     */
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

    // Helper method to create a CardDTO from a ResultSet
    private CardDTO createCardDTOFromResultSet(ResultSet resultSet) throws SQLException {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(resultSet.getString("id"));
        cardDTO.setName(resultSet.getString("name"));
        cardDTO.setDamage(resultSet.getInt("damage"));

        return cardDTO;
    }

    /**
     * Retrieves a list of cards in the user's deck.
     *
     * @param username The username of the user.
     * @return A List of CardDTO representing the cards in the user's deck. If the deck is not yet configured, returns an empty list.
     */
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

        // Check if the deck is not yet configured (contains only "null" values)
        if (cards.isEmpty() || cards.stream().allMatch(cardDTO -> cardDTO == null)) {
            // Handle the case where the deck is not yet configured
            // You can return an empty list or throw an exception based on your application's requirements
            return Collections.emptyList();
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
     * - 400 for non-unique card IDs
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
                preparedStatement.setObject(i + 1, UUID.fromString(cardIds.get(i)));
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
        // Check if each provided card is in the user's stack and not in another user's stack or deck
        for (String cardId : cardIds) {

            // If the card is not in the user's stack, is part of another user's stack, or is part of another user's deck, return false
            if (!isCardInUserStack(cardId, username)) {
                System.out.println(cardId);
                return false;
            }
        }
        // If all checks pass, return true
        return true;
    }

    /**
     * Checks if a card is present in the user's stack.
     *
     * @param cardId   The ID of the card to be checked.
     * @param username The username of the user.
     * @return True if the card is in the user's stack, false otherwise.
     */
    private boolean isCardInUserStack(String cardId, String username) {
        String query = "SELECT COUNT(*) FROM \"Stack\" WHERE username = ? AND card_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setObject(2, UUID.fromString(cardId));

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
                preparedStatement.setObject(i, UUID.fromString(cardId));
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

    /**
     * Checks if a card is present in another user's stack.
     *
     * @param cardId   The ID of the card to be checked.
     * @param username The username of the user whose stack is being checked.
     * @return True if the card is in another user's stack, false otherwise.
     */
    private boolean isCardInAnotherUserStack(String cardId, String username) {
        String query = "SELECT COUNT(*) FROM \"Stack\" WHERE username != ? AND card_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setObject(2, UUID.fromString(cardId));

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

    /**
     * Creates a new package of cards.
     *
     * @param cards The list of CardDTO representing the cards in the package.
     * @return An integer status code:
     * - 201 for success
     * - 400 for non-unique card IDs
     * - 409 for cards already in another package
     * - 500 for other failures
     */
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
            UUID packageId = UUID.randomUUID();
            preparedStatement.setObject(1, packageId);

            // Set the card IDs for each card in the package
            for (int i = 0; i < 5; i++) {
                preparedStatement.setObject(i + 2, UUID.fromString(cards.get(i).getId()));
            }

            int rowsInserted = preparedStatement.executeUpdate();

            return rowsInserted > 0 ? 201 : 500; // Return 201 for success, 500 for other failures
        } catch (SQLException e) {
            e.printStackTrace();
            return 500; // Return 500 for SQL exceptions
        }
    }

    /**
     * Creates a new card from a CardDTO object.
     *
     * @param cardDTO The CardDTO representing the card to be created.
     */
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
        String insertStmt = "INSERT INTO \"Card\" (id, name, damage, \"elementType\", specialties, \"cardType\") VALUES (?, ?, ?, ?, ?, ?)";
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
                preparedStatement.setObject(1, UUID.fromString(card.getId()));
                preparedStatement.setObject(2, UUID.fromString(card.getId()));
                preparedStatement.setObject(3, UUID.fromString(card.getId()));
                preparedStatement.setObject(4, UUID.fromString(card.getId()));
                preparedStatement.setObject(5, UUID.fromString(card.getId()));

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

    /**
     * Buys a card package for the user.
     *
     * @param username The username of the user.
     * @return A list of CardDTO representing the cards in the purchased package.
     * @throws CardRepository.InsufficientFundsException If the user has insufficient funds.
     * @throws CardRepository.CardPackageNotFoundException If no card package is available for buying.
     */
    public List<CardDTO> buyPackage(String username) throws CardRepository.InsufficientFundsException, CardRepository.CardPackageNotFoundException {
        // Select a random package from the "Package" table
        Map<UUID, List<CardDTO>> packageInfo = getRandomPackage();

        // Check if the packageInfo map is empty, indicating that no package was found
        if (packageInfo.isEmpty()) {
            throw new CardRepository.CardPackageNotFoundException("No card package available for buying");
        }

        // Extract package ID and cards from the map
        // this line is extracting the UUID (package ID) from the set of keys in the packageInfo map
        UUID purchasedPackageId = packageInfo.keySet().iterator().next();
        // this line is getting the list of cards associated with the extracted UUID (package ID) from the packageInfo map
        List<CardDTO> purchasedCards = packageInfo.get(purchasedPackageId);

        // Check if the user has enough funds
        if (userHasSufficientFunds(username)) {

            try {
                connection.setAutoCommit(false); // Start a transaction
                // Deduct the funds from the user's account
                updateUserCoins(username, getUserCoins(username) - 5);

                // Add the purchased cards to the user's stack
                addCardsToUserStack(username, purchasedCards);

                // update the "owner_username" column in the "Card" table for each purchased card with the username of the user who bought the package
                insertUsernameIntoCardTable(username, purchasedCards);

                // Delete the purchased package from the "Package" table using the specific package ID
                deletePackage(purchasedPackageId);

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
     * @return A Map containing the package ID and a list of CardDTO representing the cards in the selected package.
     */
    private Map<UUID, List<CardDTO>> getRandomPackage() {
        String query = "SELECT id, card1_id, card2_id, card3_id, card4_id, card5_id FROM \"Package\" ORDER BY RANDOM() LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                UUID packageId = resultSet.getObject("id", UUID.class);

                List<CardDTO> cards = new ArrayList<>();
                for (int i = 2; i <= 6; i++) {
                    String cardId = resultSet.getObject(i, UUID.class).toString();
                    cards.add(read(cardId)); // read() method retrieves a CardDTO by cardId
                }

                Map<UUID, List<CardDTO>> packageInfo = new HashMap<>();
                packageInfo.put(packageId, cards);
                return packageInfo;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyMap();
    }

    /**
     * Checks if the user has sufficient funds to purchase a card package.
     *
     * @param username The username of the user to check for sufficient funds.
     * @return True if the user has sufficient funds, false otherwise.
     * @throws CardRepository.InsufficientFundsException If the user does not have enough money for buying a card package.
     */
    private boolean userHasSufficientFunds(String username) throws CardRepository.InsufficientFundsException {

        double packageCost = 5;
        double userCoins = getUserCoins(username);

        if (userCoins >= packageCost) {
            return true;
        } else {
            throw new CardRepository.InsufficientFundsException("Not enough money for buying a card package");
        }
    }

    /**
     * Method to update the user's coins in the database.
     *
     * @param username      The username of the user.
     * @param updatedCoins  The updated amount of coins for the user.
     * @throws SQLException If a database access error occurs.
     */
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

    /**
     * Adds purchased cards to the user's stack in the database.
     *
     * @param username        The username of the user.
     * @param purchasedCards  The list of CardDTO representing the purchased cards.
     * @throws SQLException If a database access error occurs.
     */
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

    /**
     * Inserts the username of the user who bought the package into the "Card" table for the purchased cards.
     *
     * @param username       The username of the user who bought the package.
     * @param purchasedCards The list of CardDTO representing the purchased cards.
     * @throws SQLException If a database access error occurs.
     */
    private void insertUsernameIntoCardTable(String username, List<CardDTO> purchasedCards) throws SQLException {
        String insertQuery = "UPDATE \"Card\" SET owner_username = ? WHERE id = ?";

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

    /**
     * Deletes a purchased package from the "Package" table.
     *
     * @param packageId The ID of the package to be deleted.
     * @throws SQLException If a database access error occurs.
     */
    private void deletePackage(UUID packageId) throws SQLException {
        String deleteQuery = "DELETE FROM \"Package\" WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setObject(1, packageId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper transaction handling
        }
    }

    /**
     * Deletes unwanted cards from the user's stack in the database.
     *
     * @param username        The username of the user.
     * @param cardsToRemove   The list of CardDTO representing the cards to be removed.
     * @throws SQLException If a database access error occurs.
     */
    private void deleteCardsFromUserStack(String username, List<CardDTO> cardsToRemove) throws SQLException {

        String deleteQuery = "DELETE FROM \"Stack\" WHERE username = ? AND card_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            for (CardDTO card : cardsToRemove) {
                preparedStatement.setString(1, username);
                preparedStatement.setObject(2, UUID.fromString(card.getId()));
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper transaction handling
        }
    }

    /**
     * Adds a purchased card to the user's stack in the database.
     *
     * @param username The username of the user.
     * @param cardId   The ID of the card to be added.
     * @throws SQLException If a database access error occurs.
     */
    public void addCardToUserStack(String username, String cardId) throws SQLException {

        String insertQuery = "INSERT INTO \"Stack\" (username, card_id) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.setObject(2, UUID.fromString(cardId));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper transaction handling
        }

    }

    /**
     * Deletes an unwanted card from the user's stack in the database.
     *
     * @param username The username of the user.
     * @param cardId   The ID of the card to be removed.
     * @throws SQLException If a database access error occurs.
     */
    public void deleteCardFromUserStack(String username, String cardId) throws SQLException {

        String deleteQuery = "DELETE FROM \"Stack\" WHERE username = ? AND card_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.setObject(2, UUID.fromString(cardId));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper transaction handling
        }
    }

    /**
     * Updates the owner of a card in the database.
     *
     * @param cardId            The ID of the card.
     * @param newOwnerUsername  The username of the new owner.
     * @throws SQLException If a database access error occurs.
     */
    public void updateCardOwner(String cardId, String newOwnerUsername) throws SQLException {
        String updateQuery = "UPDATE \"Card\" SET owner_username = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setString(1, newOwnerUsername);
            preparedStatement.setObject(2, UUID.fromString(cardId));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper transaction handling
        }
    }

    /**
     * Reads a card from the database using its ID.
     *
     * @param cardId The ID of the card.
     * @return The CardDTO representing the card, or null if the card is not found.
     */
    public CardDTO read(String cardId) {
        // Implement logic to retrieve a card by its ID from the database
        String query = "SELECT * FROM \"Card\" WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, UUID.fromString(cardId));
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

}

