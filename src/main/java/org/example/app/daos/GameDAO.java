package org.example.app.daos;
import org.example.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.lang.Package;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class GameDAO {

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    public static final String WATER_GOBLIN_SPECIALTY = "WaterGoblin";
    public static final String FIRE_GOBLIN_SPECIALTY = "FireGoblin";
    public static final String REGULAR_GOBLIN_SPECIALTY = "RegularGoblin";
    public static final String WIZZARD_SPECIALTY = "Wizzard";
    public static final String KNIGHT_SPECIALTY = "Knight";
    public static final String KRAKEN_SPECIALTY = "Kraken";
    public static final String FIRE_ELF_SPECIALTY = "FireElf";
    public static final String ORK_SPECIALTY = "Ork";
    public static final String DRAGON_SPECIALTY = "Dragon";

    public GameDAO(Connection connection) {
        setConnection(connection);
    }

    public String carryOutBattle(String username1, String username2) {

        createBattle(username1, username2);

        for (int round = 1; round <= 100; round++) {
            try {
                // Select one card for each user from their decks
                Card cardUser1 = selectRandomCardFromDeck(username1);
                Card cardUser2 = selectRandomCardFromDeck(username2);

                // Implement the logic for the battle using the selected cards
                // (Apply specialties, calculate damage, compare damage, etc.)
                applySpecialty();

                // Log the battle details, winner, loser, and draw status
                logRound(username1, username2, round, cardUser1, cardUser2);

            } catch (SQLException e) {
                // Handle SQL exception
                e.printStackTrace();
            }
        }

        // Return the result of the battle
        return "Battle completed";
    }

    public void createBattle(String user1Username, String user2Username) {
        UUID battleId = UUID.randomUUID();

        String insertBattleQuery = "INSERT INTO \"Battle\"(id, user1_username, user2_username) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertBattleQuery)) {
            preparedStatement.setObject(1, battleId);
            preparedStatement.setString(2, user1Username);
            preparedStatement.setString(3, user2Username);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void logRound(String username1, String username2, int round, Card cardUser1, Card cardUser2) {
        // Implement logic to log battle details to the database
        // You need to insert data into the BattleLog, RoundDetail, and other related tables
        // Use prepared statements for database operations to prevent SQL injection
    }

    public void applySpecialty(Card cardUser1, Card cardUser2) {
        if (cardUser1.getSpecialties() != null) {
            for (String cardSpecialty : cardUser1.getSpecialties()) {
                // logic to apply the specialty effect to the card
                // This method modifies the card based on the specialty

                // Check the specialty type and apply the corresponding effect
                switch (cardSpecialty) {
                    case WATER_GOBLIN_SPECIALTY, FIRE_GOBLIN_SPECIALTY, REGULAR_GOBLIN_SPECIALTY:
                        // Goblins are too afraid of Dragons to attack
                        if (cardUser2.getSpecialties() != null &&
                                containsSpecialty(cardUser2.getSpecialties(), DRAGON_SPECIALTY)) {
                            // set damage to 0
                            cardUser1.setDamage(0);
                        }
                        break;

                    case WIZZARD_SPECIALTY:
                        // Wizzard can control Orks so they are not able to damage them
                        if (cardUser2.getSpecialties() != null &&
                                containsSpecialty(cardUser2.getSpecialties(), ORK_SPECIALTY)) {
                            // set damage to 0
                            cardUser2.setDamage(0);
                        }
                        break;

                    case KNIGHT_SPECIALTY:
                        // The armor of Knights is so heavy that WaterSpells make them drown instantly
                        if (cardUser2.getElementType().equals(ElementType.WATER)) {
                            // set damage to 0
                            cardUser2.setDamage(100);
                        }
                        break;

                    case KRAKEN_SPECIALTY:
                        // The Kraken is immune against spells
                        if (cardUser2 instanceof SpellCard) {
                            // set damage to 0
                            cardUser2.setDamage(0);
                        }
                        break;

                    case FIRE_ELF_SPECIALTY:
                        // The FireElves know Dragons since they were little and can evade their attacks
                        if (cardUser2.getSpecialties() != null &&
                                containsSpecialty(cardUser2.getSpecialties(), DRAGON_SPECIALTY)) {
                            // set damage to 0
                            cardUser2.setDamage(0);
                        }
                        break;

                    default:
                        // Handle other specialties or no effect
                        break;
                }
            }
        }
    }

    // Helper method to check if the card has a specific specialty
    protected boolean containsSpecialty(String[] specialties, String specialtyToFind) {
        for (String specialty : specialties) {
            if (specialty.equals(specialtyToFind)) {
                return true;
            }
        }
        return false;
    }

}
