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

    /**
     * Initiates a battle between two users and simulates the battle rounds.
     *
     * @param username1 The username of the first user.
     * @param username2 The username of the second user.
     * @return A detailed log of the battle rounds.
     */
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

            // Check if it's a chaos round
            if (round == 25 || round == 50 || round == 75) {
                // Calculate effective damage randomly for both users
                Double effectiveDamageUser1 = getRandomEffectiveDamage();
                Double effectiveDamageUser2 = getRandomEffectiveDamage();
                // Update the winner based on random damage
                if (effectiveDamageUser1 > effectiveDamageUser2) {
                    setWinnerAndLoser(username1, username2, user1Card, user2Card);
                } else if (effectiveDamageUser1 < effectiveDamageUser2) {
                    setWinnerAndLoser(username2, username1, user2Card, user1Card);
                } else {
                    draw = true;
                }
            } else {

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
            }

            // Log the round details, winner, loser, cards, and draw status
            logRound(battleId, round, winner, loser, winnerCard, loserCard, draw, battleLog);

            // end the battle, if one user has lost all his cards
            if (user1Deck.isEmpty() || user2Deck.isEmpty()) {
                break;
            }
        }

        // Reset variables
        setWinner(null);
        setLoser(null);
        setWinnerCard(null);
        setLoserCard(null);

        // Update decks and stacks when the battle ends
        updateDecksAndStacks();

        // Return the detailed battle log
        return "Battle completed\n" + battleLog;
    }

    /**
     * Generates a random value representing effective damage for chaos rounds.
     *
     * @return A random double value between 0 (inclusive) and 100 (inclusive)
     *         representing the effective damage.
     */
    private Double getRandomEffectiveDamage() {
        Random random = new Random();
        // Generate a random double value between 0 and 100 (inclusive) for effective damage
        return random.nextDouble() * 101;
    }

    /**
     * Creates a new battle record in the database.
     *
     * @param user1Username The username of the first user.
     * @param user2Username The username of the second user.
     * @return The unique identifier (UUID) of the created battle.
     */
    public UUID createBattle(String user1Username, String user2Username) {
        UUID battleId = UUID.randomUUID();

        String insertBattleQuery = "INSERT INTO \"Battle\"(\"id\", \"user1_username\", \"user2_username\") VALUES (?, ?, ?)";

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

    /**
     * Builds up a list of cards for a given username from the user's deck.
     *
     * @param username The username for which to build the deck list.
     * @return A list of cards from the user's deck.
     */
    private List<Card> buildUpDeckList(String username) {
        List<Card> selectedCards = new ArrayList<>();

        // Select all cards for the user from their deck
        String selectDeckQuery = "SELECT \"card1_id\", \"card2_id\", \"card3_id\", \"card4_id\" FROM \"Deck\" WHERE \"username\" = ?";
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

    /**
     * Retrieves a card from the database based on its ID.
     *
     * @param cardId The unique identifier (UUID) of the card.
     * @return The Card object representing the retrieved card or null if not found.
     */
    public Card getCardById(UUID cardId) {
        String selectCardQuery = "SELECT * FROM \"Card\" WHERE \"id\" = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectCardQuery)) {
            preparedStatement.setObject(1, cardId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    CardName cardName = CardName.valueOf(resultSet.getString("name"));
                    Double damage = resultSet.getDouble("damage");
                    String elementTypeString = resultSet.getString("elementType");
                    // Map elementTypeString to ElementType enum
                    ElementType elementType = ElementType.valueOf(elementTypeString);
                    String[] specialties = (String[]) resultSet.getArray("specialties").getArray();
                    CardType cardType = CardType.valueOf(resultSet.getString("cardType"));
                    String ownerUsername = resultSet.getString("owner_username");

                    // Create a new Card instance based on the type of card (Monster or Spell)
                    if (cardType == CardType.MONSTER) {
                        return new MonsterCard(cardId, cardName, damage, elementType, specialties, ownerUsername);
                    } else if (cardType == CardType.SPELL) {
                        return new SpellCard(cardId, cardName, damage, elementType, specialties, ownerUsername);
                    } else {
                        // other card types
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // if no card is found
    }

    /**
     * Selects a random card from the given deck.
     *
     * @param deck The deck from which to select a card.
     * @return A randomly selected card from the deck.
     */
    private Card selectRandomCardFromDeck(List<Card> deck) {
        if (deck.isEmpty()) {
            return null; // if the deck is empty
        }

        Random random = new Random();
        int randomIndex = random.nextInt(deck.size());
        Card selectedCard = deck.get(randomIndex);

        return selectedCard;
    }

    /**
     * Applies specialty effects during a battle round based on the selected cards.
     *
     * @param user1Card   The card selected by the first user.
     * @param user2Card   The card selected by the second user.
     * @param username1   The username of the first user.
     * @param username2   The username of the second user.
     */
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

    /**
     * Checks if the specialties of a card contain a specific specialty.
     *
     * @param specialties      The array of specialties of a card.
     * @param specialtyToFind  The specialty to check for.
     * @return True if the card has the specified specialty; false otherwise.
     */
    protected boolean containsSpecialty(String[] specialties, String specialtyToFind) {
        for (String specialty : specialties) {
            if (specialty.equals(specialtyToFind)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the winner and loser based on the battle outcome.
     *
     * @param winnerUsername The username of the winner.
     * @param loserUsername  The username of the loser.
     * @param winnerCard     The card of the winner.
     * @param loserCard      The card of the loser.
     */
    private void setWinnerAndLoser(String winnerUsername, String loserUsername, Card winnerCard, Card loserCard) {
        setWinner(winnerUsername);
        setLoser(loserUsername);
        setWinnerCard(winnerCard);
        setLoserCard(loserCard);

        // Update decks based on the winner and loser
        updateDecks();

        // Update user stats
        updateStats(winnerUsername, loserUsername);

        // Update Elo scores
        updateEloScores(winnerUsername, loserUsername);
    }

    /**
     * Updates the decks based on the winner and loser after a battle round.
     */
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

    /**
     * Removes a card from the given deck.
     *
     * @param deck The deck from which to remove the card.
     * @param card The card to be removed.
     */
    private void removeFromDeck(List<Card> deck, Card card) {
        deck.remove(card);
    }

    /**
     * Adds a card to the given deck.
     *
     * @param deck The deck to which the card should be added.
     * @param card The card to be added.
     */
    private void addToDeck(List<Card> deck, Card card) {
        deck.add(card);
    }

    /**
     * Updates user statistics (wins and losses) after a battle round.
     *
     * @param winnerUsername The username of the winner.
     * @param loserUsername  The username of the loser.
     */
    private void updateStats(String winnerUsername, String loserUsername) {
        try {
            // Increment wins for the winner
            incrementWins(winnerUsername);

            // Increment losses for the loser
            incrementLosses(loserUsername);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates Elo scores after a battle round.
     *
     * @param winnerUsername The username of the winner.
     * @param loserUsername  The username of the loser.
     */
    private void updateEloScores(String winnerUsername, String loserUsername) {
        try {
            // Increment Elo score for the winner and decrement for the loser
            incrementEloScore(winnerUsername, 3);
            decrementEloScore(loserUsername, 5);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increments the number of wins for the specified user in the database.
     *
     * @param username The username of the user to increment wins for.
     * @throws SQLException If a SQL exception occurs during the database update.
     */
    private void incrementWins(String username) throws SQLException {
        String updateWinsQuery = "UPDATE \"User\" SET \"wins\" = \"wins\" + 1 WHERE \"username\" = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateWinsQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Increments the number of losses for the specified user in the database.
     *
     * @param username The username of the user to increment losses for.
     * @throws SQLException If a SQL exception occurs during the database update.
     */
    private void incrementLosses(String username) throws SQLException {
        String updateLossesQuery = "UPDATE \"User\" SET \"losses\" = \"losses\" + 1 WHERE \"username\" = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateLossesQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Increments the Elo score for the specified user in the database.
     *
     * @param username The username of the user to increment the Elo score for.
     * @param increment The amount by which to increment the Elo score.
     * @throws SQLException If a SQL exception occurs during the database update.
     */
    private void incrementEloScore(String username, int increment) throws SQLException {
        String updateEloQuery = "UPDATE \"User\" SET \"elo_score\" = \"elo_score\" + ? WHERE \"username\" = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateEloQuery)) {
            preparedStatement.setInt(1, increment);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Decrements the Elo score for the specified user in the database.
     *
     * @param username  The username of the user to decrement the Elo score for.
     * @param decrement The amount by which to decrement the Elo score.
     * @throws SQLException If a SQL exception occurs during the database update.
     */
    private void decrementEloScore(String username, int decrement) throws SQLException {
        String updateEloQuery = "UPDATE \"User\" SET \"elo_score\" = \"elo_score\" - ? WHERE \"username\" = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateEloQuery)) {
            preparedStatement.setInt(1, decrement);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Logs details of a battle round, including winner, loser, and drawn status.
     *
     * @param battleId   The unique identifier (UUID) of the battle.
     * @param round      The round number.
     * @param winner     The username of the winner.
     * @param loser      The username of the loser.
     * @param winnerCard The card of the winner.
     * @param loserCard  The card of the loser.
     * @param draw       True if the round ended in a draw; false otherwise.
     */
    private void logRound(UUID battleId, Integer round, String winner, String loser, Card winnerCard, Card loserCard, boolean draw, StringBuilder battleLog) {
        try {
            // Insert into RoundDetail table
            UUID roundId = UUID.randomUUID();

            if (draw) {
                // Log round details for draw
                String drawRoundLog = String.format("Round %d ended in a draw.\n", round);
                // Insert into RoundDetail table
                insertRoundDetail(roundId, null, null, null, null, null, null);
                // Insert into RoundLog table
                insertRoundLog(battleId, round, null, null, true, roundId);
                // Append draw round log
                battleLog.append(drawRoundLog);
            } else {
                // Log round details for normal round
                insertRoundDetail(roundId, winnerCard.getId(), winnerCard.getName(), winner, loserCard.getId(), loserCard.getName(), loser);
                insertRoundLog(battleId, round, winner, loser, false, roundId);
                // Retrieve and append round details to the battle log
                battleLog.append(getRoundLogDetails(battleId, round));
            }

        } catch (SQLException e) {
            // Handle SQL exception by logging or rethrowing if necessary
            e.printStackTrace();
        }
    }

    /**
     * Inserts details of a round into the "RoundDetail" table in the database.
     *
     * @param roundId             The unique identifier (UUID) of the round.
     * @param winnerCardId       The unique identifier (UUID) of the winning card used in the round.
     * @param winnerCardName     The name of the winning card used in the round.
     * @param winnerPlayerUsername The username of the player who won the round.
     * @param loserCardId        The unique identifier (UUID) of the losing card used in the round.
     * @param loserCardName      The name of the losing card used in the round.
     * @param loserPlayerUsername The username of the player who lost the round.
     * @throws SQLException If a SQL exception occurs during the database update.
     */
    private void insertRoundDetail(UUID roundId, UUID winnerCardId, CardName winnerCardName, String winnerPlayerUsername, UUID loserCardId, CardName loserCardName, String loserPlayerUsername) throws SQLException {
        String insertRoundDetailQuery = "INSERT INTO \"RoundDetail\"(\"round_id\", \"winner_card_id\", \"winner_card_name\", \"winner_player_username\", \"loser_card_id\", \"loser_card_name\", \"loser_player_username\") VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertRoundDetailQuery)) {
            preparedStatement.setObject(1, roundId);
            preparedStatement.setObject(2, winnerCardId);
            preparedStatement.setString(3, String.valueOf(winnerCardName));
            preparedStatement.setString(4, winnerPlayerUsername);
            preparedStatement.setObject(5, loserCardId);
            preparedStatement.setString(6, String.valueOf(loserCardName));
            preparedStatement.setString(7, loserPlayerUsername);

            preparedStatement.executeUpdate();
        }
    }

    /**
     * Inserts a log entry for a round into the "RoundLog" table in the database.
     *
     * @param battleId        The unique identifier (UUID) of the battle associated with the round.
     * @param roundNumber     The round number.
     * @param winnerUsername  The username of the winner of the round.
     * @param loserUsername   The username of the loser of the round.
     * @param draw            True if the round ended in a draw; false otherwise.
     * @param roundId         The unique identifier (UUID) of the round.
     * @throws SQLException If a SQL exception occurs during the database update.
     */
    private void insertRoundLog(UUID battleId, Integer roundNumber, String winnerUsername, String loserUsername, boolean draw, UUID roundId) throws SQLException {
        String insertRoundLogQuery = "INSERT INTO \"RoundLog\"(\"battle_id\", \"round_number\", \"winner_username\", \"loser_username\", \"draw\", \"round_id\") VALUES (?, ?, ?, ?, ?, ?)";

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

    /**
     * Retrieves and formats details of a battle round from the database.
     *
     * @param battleId    The unique identifier (UUID) of the battle.
     * @param roundNumber The round number.
     * @return A formatted string containing details of the battle round.
     */
    private String getRoundLogDetails(UUID battleId, int roundNumber) {
        StringBuilder roundLogDetails = new StringBuilder();

        try {
            String selectRoundLogDetailsQuery =
                    "SELECT rd.\"winner_player_username\", rd.\"winner_card_name\", rd.\"winner_card_id\", rd.\"loser_player_username\", rd.\"loser_card_name\", rd.\"loser_card_id\", rl.\"draw\" " +
                            "FROM \"RoundLog\" rl " +
                            "JOIN \"RoundDetail\" rd ON rl.\"round_id\" = rd.\"round_id\" " +
                            "WHERE rl.\"battle_id\" = ? AND rl.\"round_number\" = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(selectRoundLogDetailsQuery)) {
                preparedStatement.setObject(1, battleId);
                preparedStatement.setInt(2, roundNumber);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    roundLogDetails.append(String.format("Round %d Details:\n", roundNumber));

                    while (resultSet.next()) {
                        String winnerPlayerUsername = resultSet.getString("winner_player_username");
                        String winnerCardName = resultSet.getString("winner_card_name");
                        UUID winnerCardId = UUID.fromString(resultSet.getString("winner_card_id"));
                        String loserPlayerUsername = resultSet.getString("loser_player_username");
                        String loserCardName = resultSet.getString("loser_card_name");
                        UUID loserCardId = UUID.fromString(resultSet.getString("loser_card_id"));
                        boolean draw = resultSet.getBoolean("draw");

                        roundLogDetails.append(String.format("  Winner: %s, Winning Card: %s, Winning Card ID: %s\n" +
                                        " Loser: %s, Losing Card: %s, Losing Card ID: %s\n" +
                                        " %s\n",
                                winnerPlayerUsername, winnerCardName, winnerCardId,
                                loserPlayerUsername, loserCardName, loserCardId,
                                (draw ? " (Draw)" : "")));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            roundLogDetails.append("Error retrieving round details.\n");
        }

        return String.valueOf(roundLogDetails);
    }

    /**
     * Updates decks and stacks after the battle ends, moving all cards to the stack.
     */
    private void updateDecksAndStacks() {
        try {
            // Remove all cards from the decks of both players at the end of the battle
            removeAllCardsFromDeck(username1);
            removeAllCardsFromDeck(username2);

            // Move all cards from the deck list to the stack of each player
            moveAllCardsToStack(username1, user1Deck);
            moveAllCardsToStack(username2, user2Deck);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes all cards from the deck of the specified username.
     *
     * @param username The username for which to remove all cards from the deck.
     */
    private void removeAllCardsFromDeck(String username) {
        String deleteDeckQuery = "DELETE FROM \"Deck\" WHERE \"username\" = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteDeckQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Moves all cards from the deck list to the stack of the specified username,
     * if they are not already present in the stack.
     *
     * @param username The username for which to move cards to the stack.
     * @param deck     The deck containing the cards to be moved.
     */
    private void moveAllCardsToStack(String username, List<Card> deck) {
        String updateCardOwnerQuery = "UPDATE \"Card\" SET \"owner_username\" = ? WHERE \"id\" = ?";

        try (PreparedStatement updateStatement = connection.prepareStatement(updateCardOwnerQuery)) {
            String insertStackQuery = "INSERT INTO \"Stack\"(\"username\", \"card_id\") VALUES (?, ?)";

            try (PreparedStatement insertStatement = connection.prepareStatement(insertStackQuery)) {
                for (Card card : deck) {
                    // Check if the card is already in the user's stack
                    if (!isCardInStack(username, card.getId())) {
                        // Update owner_username in Card table
                        updateStatement.setString(1, username);
                        updateStatement.setObject(2, card.getId());
                        updateStatement.executeUpdate();

                        // Insert into Stack table
                        insertStatement.setString(1, username);
                        insertStatement.setObject(2, card.getId());
                        insertStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a card is already present in the user's stack.
     *
     * @param username The username of the user.
     * @param cardId   The ID of the card to check.
     * @return True if the card is already in the user's stack, false otherwise.
     * @throws SQLException If a SQL exception occurs during the database query.
     */
    private boolean isCardInStack(String username, UUID cardId) throws SQLException {
        String selectStackQuery = "SELECT COUNT(*) AS count FROM \"Stack\" WHERE \"username\" = ? AND \"card_id\" = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectStackQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.setObject(2, cardId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        }

        return false; // Default to false if no result is found
    }


}
