package org.example.app.daos;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.example.*;
import java.sql.*;
import java.util.*;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class GameDAO {

    Connection connection;
    private String username1 = null;
    private String username2 = null;
    private List<Card> user1Deck = null;
    private List<Card> user2Deck = null;
    private String winner = null;
    private String loser = null;
    private Card winnerCard = null;
    private Card loserCard = null;

    public static final String WATER_GOBLIN_SPECIALTY = "WaterGoblin";
    public static final String FIRE_GOBLIN_SPECIALTY = "FireGoblin";
    public static final String REGULAR_GOBLIN_SPECIALTY = "RegularGoblin";
    public static final String WIZZARD_SPECIALTY = "Wizzard";
    public static final String KNIGHT_SPECIALTY = "Knight";
    public static final String KRAKEN_SPECIALTY = "Kraken";
    public static final String FIRE_ELF_SPECIALTY = "FireElf";
    public static final String ORK_SPECIALTY = "Ork";
    public static final String DRAGON_SPECIALTY = "Dragon";
    private static final int NUMBER_OF_ROUNDS = 100;

    public GameDAO(Connection connection) {
        setConnection(connection);
    }

    public String carryOutBattle(String username1, String username2) {

        setUsername1(username1);
        setUsername2(username2);

        UUID battleId = createBattle(username1, username2);

        StringBuilder battleLog = new StringBuilder();

        // Select deck cards at the start of each battle
        setUser1Deck(buildUpDeckList(username1));
        setUser2Deck(buildUpDeckList(username2));

        for (int round = 1; round <= NUMBER_OF_ROUNDS; round++) {

            boolean draw = false;
            // Select one card for each user from their decks
            Card user1Card = selectRandomCardFromDeck(user1Deck);
            Card user2Card = selectRandomCardFromDeck(user2Deck);

            // battle logic using the selected cards
            applySpecialty(user1Card, user2Card, username1, username2);
            applySpecialty(user2Card, user1Card, username2, username1);

            if (winner == null && loser == null) {
                Double effectiveDamageUser1 = user1Card.calculateEffectiveDamage(user2Card);
                Double effectiveDamageUser2 = user2Card.calculateEffectiveDamage(user1Card);

                if (effectiveDamageUser1 > effectiveDamageUser2) {
                    setWinnerAndLoser(username1, username2, user1Card, user2Card);
                } else if (effectiveDamageUser1 < effectiveDamageUser2) {
                    setWinnerAndLoser(username2, username1, user2Card, user1Card);
                } else {
                    draw = true;
                }
            }

            // Log the round details, winner, loser, cards, and draw status
            logRound(battleId, round, winner, loser, winnerCard, loserCard, draw);

            // Retrieve and append round details to the battle log
            battleLog.append(getRoundLogDetails(battleId, round));

            // end the battle, if one user has lost all his cards
            if (user1Deck.isEmpty() || user2Deck.isEmpty()) {
                break;
            }

            // Reset variables
            setWinner(null);
            setLoser(null);
            setWinnerCard(null);
            setLoserCard(null);

        }
        // Update decks and stacks when the battle ends
        updateDecksAndStacks();

        // Return the detailed battle log
        return "Battle completed\n" + battleLog.toString();
    }

    public UUID createBattle(String user1Username, String user2Username) {
        UUID battleId = UUID.randomUUID();

        String insertBattleQuery = "INSERT INTO \"Battle\"(id, user1_username, user2_username) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertBattleQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setObject(1, battleId);
            preparedStatement.setString(2, user1Username);
            preparedStatement.setString(3, user2Username);

            preparedStatement.executeUpdate();

            // Retrieve the generated battle ID
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return battleId;
            } else {
                throw new SQLException("Creating battle failed, no ID obtained.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Card> buildUpDeckList(String username) {
        List<Card> selectedCards = new ArrayList<>();

        // Select all cards for the user from their deck
        String selectDeckQuery = "SELECT card1_id, card2_id, card3_id, card4_id FROM Deck WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectDeckQuery)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    for (int i = 1; i <= 4; i++) {
                        UUID cardId = UUID.fromString(resultSet.getString("card" + i + "_id"));
                        Card card = getCardById(cardId);
                        if (card != null) {
                            selectedCards.add(card);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return selectedCards;
    }

    private Card selectRandomCardFromDeck(List<Card> deck) {
        if (deck.isEmpty()) {
            return null; // Handle appropriately if the deck is empty
        }

        Random random = new Random();
        int randomIndex = random.nextInt(deck.size());
        Card selectedCard = deck.get(randomIndex);

        return selectedCard;
    }

    private UUID getCardIdFromDatabase(Card card) {
        // retrieve the card ID from the database based on the card instance

        String selectCardIdQuery = "SELECT id FROM Card WHERE name = ? AND owner_username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectCardIdQuery)) {
            preparedStatement.setObject(1, card.getName());
            preparedStatement.setString(2, card.getOwner());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Handle appropriately if no card ID is found
    }

    public Card getCardById(UUID cardId) {
        String selectCardQuery = "SELECT * FROM Card WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectCardQuery)) {
            preparedStatement.setObject(1, cardId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    CardName cardName = CardName.valueOf(resultSet.getString("name"));
                    Double damage = resultSet.getDouble("damage");
                    String elementTypeString = resultSet.getString("elementType");
                    String[] specialties = (String[]) resultSet.getArray("specialties").getArray();

                    // Map elementTypeString to ElementType enum
                    ElementType elementType = ElementType.valueOf(elementTypeString);

                    // Fetch the owner of the card
                    String ownerUsername = resultSet.getString("owner_username");

                    // Create a new Card instance based on the type of card (Monster or Spell)
                    CardType cardType = CardType.valueOf(resultSet.getString("cardType"));
                    if (cardType == CardType.MONSTER) {
                        return new MonsterCard(cardId, cardName, damage, elementType, specialties, ownerUsername);
                    } else if (cardType == CardType.SPELL) {
                        return new SpellCard(cardId, cardName, damage, elementType, specialties, ownerUsername);
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

    private void logRound(UUID battleId, Integer round, String winner, String loser, Card winnerCard, Card loserCard, boolean draw) {
        try {
            // Insert into RoundDetail table
            UUID roundId = UUID.randomUUID();
            insertRoundDetail(roundId, winnerCard.getId(), winnerCard.getName(), winner);
            insertRoundDetail(roundId, loserCard.getId(), loserCard.getName(), loser);

            // Insert into RoundLog table
            insertRoundLog(battleId, round, winner, loser, draw, roundId);
        } catch (SQLException e) {
            // Handle SQL exception by logging or rethrowing if necessary
            e.printStackTrace();
        }
    }

    private void insertRoundDetail(UUID roundId, UUID cardId, CardName cardName, String playerUsername) throws SQLException {
        String insertRoundDetailQuery = "INSERT INTO \"RoundDetail\"(round_id, card_id, card_name, player_username) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertRoundDetailQuery)) {
            preparedStatement.setObject(1, roundId);
            preparedStatement.setObject(2, cardId);
            preparedStatement.setObject(3, cardName);
            preparedStatement.setString(4, playerUsername);

            preparedStatement.executeUpdate();
        }
    }

    private void insertRoundLog(UUID battleId, Integer roundNumber, String winnerUsername, String loserUsername, boolean draw, UUID roundId) throws SQLException {
        String insertRoundLogQuery = "INSERT INTO \"RoundLog\"(battle_id, round_number, winner_username, loser_username, draw, round_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertRoundLogQuery)) {
            preparedStatement.setObject(1, battleId);
            preparedStatement.setInt(2, roundNumber);
            preparedStatement.setString(3, winnerUsername);
            preparedStatement.setString(4, loserUsername);
            preparedStatement.setBoolean(5, draw);
            preparedStatement.setObject(6, roundId);

            preparedStatement.executeUpdate();
        }
    }

    // Helper function to retrieve and format round details from the database
    private String getRoundLogDetails(UUID battleId, int roundNumber) {
        StringBuilder roundLogDetails = new StringBuilder();

        try {
            String selectRoundLogDetailsQuery =
                    "SELECT rd.player_username, rd.card_name, rd.card_id, rl.draw " +
                            "FROM \"RoundLog\" rl " +
                            "JOIN \"RoundDetail\" rd ON rl.round_id = rd.round_id " +
                            "WHERE rl.battle_id = ? AND rl.round_number = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(selectRoundLogDetailsQuery)) {
                preparedStatement.setObject(1, battleId);
                preparedStatement.setInt(2, roundNumber);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    roundLogDetails.append(String.format("Round %d Details:\n", roundNumber));

                    while (resultSet.next()) {
                        String playerUsername = resultSet.getString("player_username");
                        String cardName = resultSet.getString("card_name");
                        UUID cardId = UUID.fromString(resultSet.getString("card_id"));
                        boolean draw = resultSet.getBoolean("draw");

                        roundLogDetails.append(String.format("  Player: %s, Card: %s, Card ID: %s%s\n",
                                playerUsername, cardName, cardId, (draw ? " (Draw)" : "")));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            roundLogDetails.append("Error retrieving round details.\n");
        }

        return roundLogDetails.toString();
    }

    public void applySpecialty(Card user1Card, Card user2Card, String username1,  String username2) {
        if (user1Card.getSpecialties() != null) {
            for (String cardSpecialty : user1Card.getSpecialties()) {
                // logic to apply the specialty effect to the card
                // This method modifies the card based on the specialty

                // Check the specialty type and apply the corresponding effect
                switch (cardSpecialty) {
                    case WATER_GOBLIN_SPECIALTY, FIRE_GOBLIN_SPECIALTY, REGULAR_GOBLIN_SPECIALTY:
                        // Goblins are too afraid of Dragons to attack
                        if (user2Card.getSpecialties() != null &&
                                containsSpecialty(user2Card.getSpecialties(), DRAGON_SPECIALTY)) {
                            setWinnerAndLoser(username2, username1, user2Card, user1Card);
                        }
                        break;

                    case WIZZARD_SPECIALTY:
                        // Wizzard can control Orks so they are not able to damage them
                        if (user2Card.getSpecialties() != null &&
                                containsSpecialty(user2Card.getSpecialties(), ORK_SPECIALTY)) {
                            setWinnerAndLoser(username1, username2, user1Card, user2Card);
                        }
                        break;

                    case KNIGHT_SPECIALTY:
                        // The armor of Knights is so heavy that WaterSpells make them drown instantly
                        if (user2Card.getCardType().equals(CardType.SPELL) && user2Card.getElementType().equals(ElementType.WATER)) {
                            setWinnerAndLoser(username2, username1, user2Card, user1Card);
                        }
                        break;

                    case KRAKEN_SPECIALTY:
                        // The Kraken is immune against spells
                        if (user2Card instanceof SpellCard) {
                            setWinnerAndLoser(username1, username2, user1Card, user2Card);
                        }
                        break;

                    case FIRE_ELF_SPECIALTY:
                        // The FireElves know Dragons since they were little and can evade their attacks
                        if (user2Card.getSpecialties() != null &&
                                containsSpecialty(user2Card.getSpecialties(), DRAGON_SPECIALTY)) {
                            setWinnerAndLoser(username1, username2, user1Card, user2Card);
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

    private void setWinnerAndLoser(String winnerUsername, String loserUsername, Card winnerCard, Card loserCard) {
        setWinner(winnerUsername);
        setLoser(loserUsername);
        setWinnerCard(winnerCard);
        setLoserCard(loserCard);

        // Update decks based on the winner and loser
        updateDecks();
    }

    private void updateDecks() {
        try {
            // Remove defeated cards from the loser's deck and add to the winner's deck
            if (winner.equals(username1)) {
                // Move the card from the loser's deck to the winner's deck
                removeFromDeck(user2Deck, loserCard);
                addToDeck(user1Deck, loserCard);
            } else if (winner.equals(username2)) {
                removeFromDeck(user1Deck, loserCard);
                addToDeck(user2Deck, loserCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeFromDeck(List<Card> deck, Card card) {
        deck.remove(card);
    }

    private void addToDeck(List<Card> deck, Card card) {
        deck.add(card);
    }

    private void updateDecksAndStacks() {
        try {
            // Remove all cards from the decks of both players
            removeAllCardsFromDeck(username1);
            removeAllCardsFromDeck(username2);

            // Move all cards from the deck list to the stack of each player
            moveAllCardsToStack(username1, user1Deck);
            moveAllCardsToStack(username2, user2Deck);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeAllCardsFromDeck(String username) {
        String deleteDeckQuery = "DELETE FROM \"Deck\" WHERE username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteDeckQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void moveAllCardsToStack(String username, List<Card> deck) {
        String updateCardOwnerQuery = "UPDATE \"Card\" SET owner_username = ? WHERE id = ?";

        try (PreparedStatement updateStatement = connection.prepareStatement(updateCardOwnerQuery)) {
            String insertStackQuery = "INSERT INTO \"Stack\"(username, card_id) VALUES (?, ?)";

            try (PreparedStatement insertStatement = connection.prepareStatement(insertStackQuery)) {
                for (Card card : deck) {
                    // Update owner_username in Card table
                    updateStatement.setString(1, username);
                    updateStatement.setObject(2, getCardIdFromDatabase(card));
                    updateStatement.executeUpdate();

                    // Insert into Stack table
                    insertStatement.setString(1, username);
                    insertStatement.setObject(2, getCardIdFromDatabase(card));
                    insertStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
