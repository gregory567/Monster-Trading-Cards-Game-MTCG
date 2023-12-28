package org.example.app.daos;
import org.example.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.dtos.CardDTO;
import org.example.app.repositories.GameRepository;

import java.lang.Package;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class GameDAO {

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    public GameDAO(Connection connection) {
        setConnection(connection);
    }

    public String carryOutBattle(String username1, String username2) {
        for (int round = 1; round <= 100; round++) {
            try {
                // Select one card for each user from their decks
                Card cardUser1 = selectRandomCardFromDeck(username1);
                Card cardUser2 = selectRandomCardFromDeck(username2);

                // Implement the logic for the battle using the selected cards
                // (Apply specialties, calculate damage, compare damage, etc.)

                // Log the battle details, winner, loser, and draw status
                logBattleDetails(username1, username2, round, cardUser1, cardUser2);

            } catch (SQLException e) {
                // Handle SQL exception
                e.printStackTrace();
            }
        }

        // Return the result of the battle
        return "Battle completed";
    }

    private Card selectRandomCardFromDeck(String username) throws SQLException {
        String selectCardQuery = "SELECT card_id FROM Deck WHERE username = ? ORDER BY RANDOM() LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectCardQuery)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    UUID cardId = UUID.fromString(resultSet.getString("card_id"));
                    // Retrieve the card details using cardId
                    Card card = getCardById(cardId);
                    return card; // Replace with the actual card
                }
            }
        }

        return null; // Handle appropriately if no card is selected
    }

    public Card getCardById(UUID cardId) {
        String selectCardQuery = "SELECT * FROM Card WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectCardQuery)) {
            preparedStatement.setObject(1, cardId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    CardName cardName = CardName.valueOf(resultSet.getString("name"));
                    int damage = resultSet.getInt("damage");
                    String elementTypeString = resultSet.getString("elementType");
                    String[] specialties = (String[]) resultSet.getArray("specialties").getArray();

                    // Map elementTypeString to ElementType enum
                    ElementType elementType = ElementType.valueOf(elementTypeString);

                    // Fetch the owner of the card
                    String ownerUsername = resultSet.getString("owner_username");

                    // Create a new Card instance based on the type of card (Monster or Spell)
                    CardType cardType = CardType.valueOf(resultSet.getString("cardType"));
                    if (cardType == CardType.MONSTER) {
                        return new MonsterCard(cardName, damage, elementType, specialties, ownerUsername);
                    } else if (cardType == CardType.SPELL) {
                        return new SpellCard(cardName, damage, elementType, specialties, ownerUsername);
                    } else {
                        // Handle other card types as needed
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Handle appropriately if no card is found
    }

    private void logBattleDetails(String username1, String username2, int round, Card cardUser1, Card cardUser2) {
        // Implement logic to log battle details to the database
        // You need to insert data into the BattleLog, RoundDetail, and other related tables
        // Use prepared statements for database operations to prevent SQL injection
    }

}
