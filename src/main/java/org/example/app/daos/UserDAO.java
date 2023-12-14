package org.example.app.daos;


import org.example.app.models.User;
import org.example.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements DAO<User> {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    @Setter(AccessLevel.PRIVATE)
    ArrayList<User> usersCache;

    TokenValidator tokenValidator;

    public UserDAO(Connection connection) {
        setConnection(connection);
    }

    // Method to create a new user in the database
    @Override
    public Integer create(String username, String password) {
        // Check if the user already exists
        if (userExists(username)) {
            // User with the same username already registered.
            System.out.println("User with the same username already registered.");
            // Return 0 to indicate unsuccessful user creation
            return 0;
        }

        // SQL statement to insert a new user into the usercredentials table
        String insertStmt = "INSERT INTO usercredentials (username, password) VALUES (?, ?);";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(insertStmt)) {
            // Set parameters in the prepared statement
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            // Execute the SQL update statement to insert the new user
            preparedStatement.executeUpdate();

            // Close the database connection
            getConnection().close();

            // Clear the user cache to ensure the latest data is retrieved on subsequent queries
            clearCache();

            // Return 1 to indicate successful user creation
            return 1;
        } catch (SQLException e) {
            // Print any SQL exception that occurs during user creation
            e.printStackTrace();
            // Return 0 to indicate unsuccessful user creation
            return 0;
        }
    }

    // Helper method to check if a user already exists in the database
    private boolean userExists(String username) {
        // SQL statement to count the number of users with the specified username
        String selectStmt = "SELECT COUNT(*) FROM usercredentials WHERE username = ?;";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(selectStmt)) {
            // Set the username parameter in the prepared statement
            preparedStatement.setString(1, username);

            // Execute the SQL query and obtain the result set
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if the result set has any rows
                if (resultSet.next()) {
                    // Retrieve the count of users with the specified username
                    int count = resultSet.getInt(1);
                    // Return true if the count is greater than 0, indicating that the user already exists
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            // Print any SQL exception that occurs during the user existence check
            e.printStackTrace();
        }

        // Return false if an exception occurred or no user with the specified username was found
        return false;
    }

    @Override
    public ArrayList<User> readAll() {
        ArrayList<User> users = new ArrayList<>();

        if (usersCache != null) {
            System.out.println("Cache hit");
            return usersCache;
        }

        String selectStmt = "SELECT * FROM userdata;";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(selectStmt);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                User user = createUserFromResultSet(resultSet);
                initializeUserData(user);
                users.add(user);
            }

            setUsersCache(users);
            getConnection().close();
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public User read(String username) {
        String selectStmt = "SELECT * FROM userdata WHERE username = ?;";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(selectStmt)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                User foundUser = createUserFromResultSet(resultSet);
                initializeUserData(foundUser);
                return foundUser;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isAuthorized(String token, String username) {
        return tokenValidator.validateToken(token, username);
    }

    @Override
    public Integer updateUser(String username, String token, String name, String bio, String image) {
        if (token == null || !isAuthorized(token, username)) {
            // Access token is missing or invalid
            return 401;
        }

        // Check if the user exists
        if (!userExists(username)) {
            // User not found
            return 404;
        }

        // Update the user data
        String updateStmt = "UPDATE userdata SET name = ?, bio = ?, image = ? WHERE username = ?;";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(updateStmt)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, bio);
            preparedStatement.setString(3, image);
            preparedStatement.setString(4, username);

            int rowsUpdated = preparedStatement.executeUpdate();

            // Clear the cache only if the update was successful
            if (rowsUpdated > 0) {
                clearCache();
                // User successfully updated
                return 200;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Return 500 for a generic error status if an exception occurred
            return 500;
        }

        // Return 500 for a generic error status if the update was unsuccessful (no rows updated)
        return 500;
    }


    @Override
    public void delete(String username) {
        String deleteStmt = "DELETE FROM usercredentials WHERE username = ?;";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(deleteStmt)) {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
            clearCache();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to initialize Stack, Deck, BattleResults, InitiatedTrades, and AcceptedTrades of the user
    private void initializeUserData(User user) {
        user.setStack(initStack(user.getUsername()));
        user.setDeck(initDeck(user.getUsername()));
        user.setBattleResults(initBattleResults(user.getUsername()));
        user.setInitiatedTrades(initTrades(user.getUsername(), "offeringUser_username"));
        user.setAcceptedTrades(initTrades(user.getUsername(), "status = 'ACCEPTED' AND offeringUser_username != ?"));
    }

    private List<TradeDeal> initTrades(String username, String condition) {
        List<TradeDeal> trades = new ArrayList<>();

        String selectTradesStmt = "SELECT * FROM TradeDeal WHERE " + condition;
        try (PreparedStatement tradesStatement = getConnection().prepareStatement(selectTradesStmt)) {
            tradesStatement.setString(1, username);
            ResultSet tradesResultSet = tradesStatement.executeQuery();

            while (tradesResultSet.next()) {
                TradeDeal tradeDeal = createTradeDealFromResultSet(tradesResultSet);
                trades.add(tradeDeal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return trades;
    }

    private void clearCache() {
        setUsersCache(null);
    }

    // Helper method to create a User instance from a ResultSet
    private User createUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User(
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getDouble("coins"),
                null, // stack will be initialized later
                null, // deck will be initialized later
                new Profile(
                        resultSet.getString("profile_name"),
                        resultSet.getString("profile_email"),
                        resultSet.getString("profile_other_details")
                ),
                resultSet.getInt("elo_score"),
                new BattleResult[0], // empty array
                new ArrayList<>(),   // empty list for initiatedTrades
                new ArrayList<>()    // empty list for acceptedTrades
        );

        return user;
    }

    // Helper method to initialize Stack for a user
    private Stack initStack(String username) {
        Stack stack = new Stack();

        String selectStackStmt = "SELECT card_id FROM Stack WHERE username = ?;";
        try (PreparedStatement stackStatement = getConnection().prepareStatement(selectStackStmt)) {
            stackStatement.setString(1, username);
            ResultSet stackResultSet = stackStatement.executeQuery();

            while (stackResultSet.next()) {
                int cardId = stackResultSet.getInt("card_id");
                Card card = getCardById(cardId);
                if (card != null) {
                    stack.attainCard(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stack;
    }

    // Helper method to initialize Deck for a user
    private Deck initDeck(String username) {
        Deck deck = new Deck();

        String selectDeckStmt = "SELECT card_id FROM Deck WHERE username = ?;";
        try (PreparedStatement deckStatement = getConnection().prepareStatement(selectDeckStmt)) {
            deckStatement.setString(1, username);
            ResultSet deckResultSet = deckStatement.executeQuery();

            while (deckResultSet.next()) {
                int cardId = deckResultSet.getInt("card_id");
                Card card = getCardById(cardId);
                if (card != null) {
                    deck.addCardToDeck(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return deck;
    }

    // Helper method to get Card by ID
    private Card getCardById(int cardId) {
        String selectCardStmt = "SELECT * FROM Card WHERE id = ?;";

        try (PreparedStatement cardStatement = getConnection().prepareStatement(selectCardStmt)) {
            cardStatement.setInt(1, cardId);

            try (ResultSet cardResultSet = cardStatement.executeQuery()) {
                if (cardResultSet.next()) {
                    String cardType = cardResultSet.getString("cardType");
                    String name = cardResultSet.getString("name");
                    int damage = cardResultSet.getInt("damage");
                    String elementType = cardResultSet.getString("elementType");
                    String[] specialties = (String[]) cardResultSet.getArray("specialties").getArray();

                    User owner = getUserByUsername(cardResultSet.getString("owner_username"));

                    switch (cardType) {
                        case "Monster":
                            return new MonsterCard(name, damage, elementType, specialties, owner);
                        case "Spell":
                            return new SpellCard(name, damage, elementType, specialties, owner);
                        default:
                            // Handle unknown card type
                            break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // Helper method to initialize BattleResults for a user
    private BattleResult[] initBattleResults(String username) {
        List<BattleResult> battleResults = new ArrayList<>();

        String selectBattleResultsStmt = "SELECT * FROM BattleResult WHERE opponent_username = ?;";
        try (PreparedStatement battleResultsStatement = getConnection().prepareStatement(selectBattleResultsStmt)) {
            battleResultsStatement.setString(1, username);
            ResultSet battleResultsResultSet = battleResultsStatement.executeQuery();

            while (battleResultsResultSet.next()) {
                BattleResult battleResult = new BattleResult(
                        getOpponentByUsername(username),
                        battleResultsResultSet.getString("outcome")
                );
                battleResults.add(battleResult);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return battleResults.toArray(new BattleResult[0]);
    }

    // Helper method to get opponent user by username
    private User getOpponentByUsername(String username) {
        String selectOpponentStmt = "SELECT * FROM BattleResult WHERE opponent_username = ?;";
        try (PreparedStatement opponentStatement = getConnection().prepareStatement(selectOpponentStmt)) {
            opponentStatement.setString(1, username);
            ResultSet opponentResultSet = opponentStatement.executeQuery();

            if (opponentResultSet.next()) {
                return createUserFromResultSet(opponentResultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Helper method to initialize InitiatedTrades for a user
    private List<TradeDeal> initInitiatedTrades(String username) {
        List<TradeDeal> initiatedTrades = new ArrayList<>();

        String selectInitiatedTradesStmt = "SELECT * FROM TradeDeal WHERE offeringUser_username = ?;";
        try (PreparedStatement initiatedTradesStatement = getConnection().prepareStatement(selectInitiatedTradesStmt)) {
            initiatedTradesStatement.setString(1, username);
            ResultSet initiatedTradesResultSet = initiatedTradesStatement.executeQuery();

            while (initiatedTradesResultSet.next()) {
                // Create TradeDeal instance and add to the list
                TradeDeal tradeDeal = createTradeDealFromResultSet(initiatedTradesResultSet);
                initiatedTrades.add(tradeDeal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return initiatedTrades;
    }

    // Helper method to initialize AcceptedTrades for a user
    private List<TradeDeal> initAcceptedTrades(String username) {
        List<TradeDeal> acceptedTrades = new ArrayList<>();

        String selectAcceptedTradesStmt = "SELECT * FROM TradeDeal WHERE status = 'ACCEPTED' AND offeringUser_username != ?;";
        try (PreparedStatement acceptedTradesStatement = getConnection().prepareStatement(selectAcceptedTradesStmt)) {
            acceptedTradesStatement.setString(1, username);
            ResultSet acceptedTradesResultSet = acceptedTradesStatement.executeQuery();

            while (acceptedTradesResultSet.next()) {
                // Create TradeDeal instance and add to the list
                TradeDeal tradeDeal = createTradeDealFromResultSet(acceptedTradesResultSet);
                acceptedTrades.add(tradeDeal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return acceptedTrades;
    }

    // Helper method to create a TradeDeal instance from a ResultSet
    private TradeDeal createTradeDealFromResultSet(ResultSet resultSet) throws SQLException {
        String offeringUserUsername = resultSet.getString("offeringUser_username");
        Card offeredCard = getCardById(resultSet.getInt("offeredCard_id"));

        CardType requirementCardType = CardType.valueOf(resultSet.getString("requirement_cardType"));
        Integer requirementMinDamage = resultSet.getInt("requirement_minDamage");
        Requirement requirement = new Requirement(requirementCardType, requirementMinDamage);

        User offeringUser = getUserByUsername(offeringUserUsername);

        return new TradeDeal(offeringUser, offeredCard, requirement);
    }

    // Helper method to get User by username
    private User getUserByUsername(String username) {
        String selectUserStmt = "SELECT * FROM users WHERE username = ?;";
        try (PreparedStatement userStatement = getConnection().prepareStatement(selectUserStmt)) {
            userStatement.setString(1, username);
            ResultSet userResultSet = userStatement.executeQuery();

            if (userResultSet.next()) {
                return createUserFromResultSet(userResultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}


