package org.example.app.daos;

import org.example.app.dtos.UserStatDTO;
import org.example.app.models.User;
import org.example.app.dtos.UserDataDTO;
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
import java.util.UUID;

public class UserDAO {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    @Setter(AccessLevel.PRIVATE)
    ArrayList<UserDataDTO> usersCache;

    public UserDAO(Connection connection) {
        setConnection(connection);
    }

    /**
     * Creates a new user in the database.
     *
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @return HTTP status code indicating the result of user creation (201 for success, 409 for conflict, 500 for error).
     */
    public Integer createUser(String username, String password) {
        // Check if the user already exists
        if (userExists(username)) {
            // User with the same username already registered.
            return 409;
        }

        // create token for later login
        String token = username + "-mtcgToken";
        Double coins = 20.0;
        Integer elo_score = 100;
        Integer wins = 0;
        Integer losses = 0;
        // SQL statement to insert a new user into the usercredentials table
        String insertStmt = "INSERT INTO \"User\" (username, password, token, coins, elo_score, wins, losses) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(insertStmt)) {
            // Set parameters in the prepared statement
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, token);
            preparedStatement.setDouble(4, coins);
            preparedStatement.setInt(5, elo_score);
            preparedStatement.setInt(6, wins);
            preparedStatement.setInt(7, losses);

            // Execute the SQL update statement to insert the new user
            preparedStatement.executeUpdate();

            // Call the helper method to insert the new user into the "Deck" table
            insertNewUserIntoDeck(username);

            // Return 201 to indicate successful user creation
            return 201;
        } catch (SQLException e) {
            // Print any SQL exception that occurs during user creation
            e.printStackTrace();
            // Return 500 to indicate unsuccessful user creation
            return 500;
        }
    }

    /**
     * Checks if a user with the given username already exists in the database.
     *
     * @param username The username to check for existence.
     * @return True if the user exists; false otherwise.
     */
    private boolean userExists(String username) {
        // SQL statement to count the number of users with the specified username
        String selectStmt = "SELECT COUNT(*) FROM \"User\" WHERE username = ?;";
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

    /**
     * Helper method to insert a new username into the "Deck" table with all card entries set to null.
     *
     * @param username The username to be inserted into the "Deck" table.
     * @throws SQLException If a database access error occurs.
     */
    private void insertNewUserIntoDeck(String username) throws SQLException {
        String insertQuery = "INSERT INTO \"Deck\" (username, card1_id, card2_id, card3_id, card4_id) VALUES (?, NULL, NULL, NULL, NULL)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper transaction handling
        }
    }

    /**
     * Retrieves a list of user data from the database.
     *
     * @return List of UserDataDTO containing profile information for all users.
     */
    public ArrayList<UserDataDTO> getUsers() {
        ArrayList<UserDataDTO> users = new ArrayList<>();

        if (usersCache != null) {
            System.out.println("Cache hit");
            return usersCache;
        }

        String selectStmt = "SELECT profile_name, profile_bio, profile_image FROM \"User\";";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(selectStmt);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                UserDataDTO userdata = createUserdataFromResultSet(resultSet);
                users.add(userdata);
            }

            setUsersCache(users);
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves user data for a specific user from the database.
     *
     * @param username The username of the user to retrieve data for.
     * @return UserDataDTO containing profile information for the specified user.
     */
    public UserDataDTO getUser(String username) {
        String selectStmt = "SELECT profile_name, profile_bio, profile_image FROM \"User\" WHERE username = ?;";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(selectStmt)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                UserDataDTO foundUser = createUserdataFromResultSet(resultSet);
                return foundUser;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates user profile information in the database.
     *
     * @param username The username of the user to update.
     * @param name     The new name for the user.
     * @param bio      The new biography for the user.
     * @param image    The new profile image URL for the user.
     * @return HTTP status code indicating the result of the update (200 for success, 404 for not found, 500 for error).
     */
    public Integer updateUser(String username, String name, String bio, String image) {

        // Check if the user exists
        if (!userExists(username)) {
            // User not found
            return 404;
        }

        // Update the user data
        String updateStmt = "UPDATE \"User\" SET profile_name = ?, profile_bio = ?, profile_image = ? WHERE username = ?;";
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

    /**
     * Authenticates a user by checking the provided username and password.
     *
     * @param username The username of the user to authenticate.
     * @param password The password provided for authentication.
     * @return User token if authentication is successful; "401" for authentication failure; "500" for error.
     */
    public String loginUser(String username, String password) {
        try {
            if (!userExists(username)) {
                // User not found
                return "404";
            } else if (!passwordMatches(username, password)) {
                // Authentication failed
                System.out.println("Authentication failed.");
                return "401";
            } else {
                // Authentication successful
                return retrieveUserToken(username);
            }
        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
            return "500";
        }
    }

    /**
     * Helper method to retrieve the user token from the database.
     *
     * @param username The username for which to retrieve the token.
     * @return The user token retrieved from the database.
     * @throws SQLException If a SQL exception occurs during token retrieval.
     */
    private String retrieveUserToken(String username) throws SQLException {
        String selectStmt = "SELECT token FROM \"User\" WHERE username = ?;";

        try (PreparedStatement preparedStatement = getConnection().prepareStatement(selectStmt)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if the result set has any rows
                if (resultSet.next()) {
                    // Retrieve the stored token from the database
                    return resultSet.getString("token");
                }
            }
        }

        // Return "404" if no token is found (should not happen if the user exists)
        return "404";
    }

    /**
     * Checks if the provided password matches the stored password for the given username.
     *
     * @param username The username for which to check the password.
     * @param password The password to compare against the stored password.
     * @return True if the provided password matches the stored password, false otherwise.
     */
    public boolean passwordMatches(String username, String password) {
        // SQL statement to retrieve the password for the given username
        String selectStmt = "SELECT password FROM \"User\" WHERE username = ?;";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(selectStmt)) {
            // Set parameters in the prepared statement
            preparedStatement.setString(1, username);

            // Execute the SQL query and obtain the result set
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if the result set has any rows
                if (resultSet.next()) {
                    // Retrieve the stored password from the database
                    String storedPassword = resultSet.getString("password");
                    // Compare the stored password with the provided password
                    return storedPassword.equals(password);
                }
            }
        } catch (SQLException e) {
            // Print any SQL exception that occurs during the password retrieval
            e.printStackTrace();
        }

        // Return false if an exception occurred or no password for the specified username was found
        return false;
    }

    /**
     * Deletes a user from the database.
     *
     * @param username The username of the user to delete.
     */
    public void deleteUser(String username) {
        String deleteStmt = "DELETE FROM \"User\" WHERE username = ?;";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(deleteStmt)) {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
            clearCache();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearCache() {
        setUsersCache(null);
    }

    /**
     * Helper method to create a User instance from a ResultSet.
     *
     * @param resultSet The ResultSet containing user data.
     * @return User instance created from the ResultSet.
     * @throws SQLException If a SQL exception occurs during the creation.
     */
    private User createUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User(
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getString("token"),
                initStack(resultSet.getString("username")),
                initDeck(resultSet.getString("username")),
                new Profile(
                        resultSet.getString("profile_name"),
                        resultSet.getString("profile_email"),
                        resultSet.getString("profile_other_details")
                ),
                resultSet.getDouble("coins"),
                resultSet.getInt("elo_score"),
                resultSet.getInt("wins"),
                resultSet.getInt("losses")
        );

        return user;
    }

    /**
     * Helper method to create a UserDataDTO instance from a ResultSet.
     *
     * @param resultSet The ResultSet containing user data.
     * @return UserDataDTO instance created from the ResultSet.
     * @throws SQLException If a SQL exception occurs during the creation.
     */
    private UserDataDTO createUserdataFromResultSet(ResultSet resultSet) throws SQLException {
        UserDataDTO userdata = new UserDataDTO(
                resultSet.getString("name"),
                resultSet.getString("bio"),
                resultSet.getString("image")
        );

        return userdata;
    }

    /**
     * Helper method to initialize Stack for a user.
     *
     * @param username The username of the user to initialize the stack for.
     * @return Stack instance containing cards from the user's stack.
     */
    private Stack initStack(String username) {
        Stack stack = new Stack();

        String selectStackStmt = "SELECT card_id FROM Stack WHERE username = ?;";
        try (PreparedStatement stackStatement = getConnection().prepareStatement(selectStackStmt)) {
            stackStatement.setString(1, username);
            ResultSet stackResultSet = stackStatement.executeQuery();

            while (stackResultSet.next()) {
                UUID cardId = (UUID) stackResultSet.getObject("card_id");
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

    /**
     * Helper method to initialize Deck for a user.
     *
     * @param username The username of the user to initialize the deck for.
     * @return Deck instance containing cards from the user's deck.
     */
    private Deck initDeck(String username) {
        Deck deck = new Deck();

        String selectDeckStmt = "SELECT card1_id, card2_id, card3_id, card4_id FROM Deck WHERE username = ?;";
        try (PreparedStatement deckStatement = getConnection().prepareStatement(selectDeckStmt)) {
            deckStatement.setString(1, username);
            ResultSet deckResultSet = deckStatement.executeQuery();

            while (deckResultSet.next()) {
                UUID card1Id = (UUID) deckResultSet.getObject("card1_id");
                UUID card2Id = (UUID) deckResultSet.getObject("card2_id");
                UUID card3Id = (UUID) deckResultSet.getObject("card3_id");
                UUID card4Id = (UUID) deckResultSet.getObject("card4_id");

                Card card1 = getCardById(card1Id);
                Card card2 = getCardById(card2Id);
                Card card3 = getCardById(card3Id);
                Card card4 = getCardById(card4Id);

                if (card1 != null && card2 != null && card3 != null && card4 != null) {
                    deck.addCardToDeck(card1);
                    deck.addCardToDeck(card2);
                    deck.addCardToDeck(card3);
                    deck.addCardToDeck(card4);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return deck;
    }

    /**
     * Helper method to get a Card by ID from the database.
     *
     * @param cardId The ID of the card to retrieve.
     * @return Card instance retrieved from the database.
     */
    public Card getCardById(UUID cardId) {
        String selectCardQuery = "SELECT * FROM Card WHERE id = ?";

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
     * Retrieves user statistics (profile name, ELO score, wins, losses) from the database.
     *
     * @param username The username of the user to retrieve statistics for.
     * @return UserStatDTO containing statistics for the specified user.
     */
    public UserStatDTO getStats(String username) {
        String selectStatsStmt = "SELECT profile_name, elo_score, wins, losses FROM \"User\" WHERE username = ?;";

        try (PreparedStatement statsStatement = getConnection().prepareStatement(selectStatsStmt)) {
            statsStatement.setString(1, username);

            try (ResultSet statsResultSet = statsStatement.executeQuery()) {
                if (statsResultSet.next()) {
                    String name = statsResultSet.getString("profile_name");
                    String eloScore = statsResultSet.getString("elo_score");
                    String wins = statsResultSet.getString("wins");
                    String losses = statsResultSet.getString("losses");

                    return new UserStatDTO(name, eloScore, wins, losses);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a scoreboard of user statistics ordered by ELO score from the database.
     *
     * @return List of UserStatDTO representing the scoreboard.
     */
    public List<UserStatDTO> getScoreBoard() {
        List<UserStatDTO> scoreBoard = new ArrayList<>();

        // SQL statement to retrieve user stats ordered by ELO
        String selectScoreBoardStmt = "SELECT profile_name, elo_score, wins, losses FROM \"User\" ORDER BY elo_score DESC;";
        try (PreparedStatement scoreBoardStatement = getConnection().prepareStatement(selectScoreBoardStmt);
             ResultSet scoreBoardResultSet = scoreBoardStatement.executeQuery()) {

            while (scoreBoardResultSet.next()) {
                UserStatDTO userStat = createUserStatFromResultSet(scoreBoardResultSet);
                scoreBoard.add(userStat);
            }

            return scoreBoard;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Helper method to create a UserStatDTO instance from a ResultSet.
     *
     * @param resultSet The ResultSet containing user statistics.
     * @return UserStatDTO instance created from the ResultSet.
     * @throws SQLException If a SQL exception occurs during the creation.
     */
    private UserStatDTO createUserStatFromResultSet(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("profile_name");
        String eloScore = resultSet.getString("elo_score");
        String wins = resultSet.getString("wins");
        String losses = resultSet.getString("losses");

        return new UserStatDTO(name, eloScore, wins, losses);
    }

}


