package org.example.app.daos;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.dtos.TradeDealDTO;

import java.sql.*;
import java.util.*;

public class TradeDealDAO {

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    @Setter(AccessLevel.PRIVATE)
    ArrayList<TradeDealDTO> tradeDealCache;

    public TradeDealDAO(Connection connection) {
        setConnection(connection);
    }

    /**
     * Retrieves a list of all trade deals from the database.
     *
     * @return A list of TradeDealDTO representing trade deals.
     */
    public List<TradeDealDTO> getTradeDeals() {
        String sql = "SELECT * FROM \"TradeDeal\"";
        List<TradeDealDTO> tradeDeals = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                TradeDealDTO tradeDealDTO = mapResultSetToTradeDealDTO(resultSet);
                tradeDeals.add(tradeDealDTO);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tradeDeals;
    }

    /**
     * Maps a ResultSet to a TradeDealDTO.
     *
     * @param resultSet The ResultSet from a database query.
     * @return A TradeDealDTO representing the trade deal.
     * @throws SQLException If a SQL exception occurs.
     */
    private TradeDealDTO mapResultSetToTradeDealDTO(ResultSet resultSet) throws SQLException {
        TradeDealDTO tradeDealDTO = new TradeDealDTO();
        tradeDealDTO.setId(resultSet.getString("id"));
        tradeDealDTO.setCardToTrade(resultSet.getString("offeredCard_id"));
        tradeDealDTO.setCardType(resultSet.getString("requirement_cardType"));
        tradeDealDTO.setMinimumDamage(resultSet.getDouble("requirement_minDamage"));
        return tradeDealDTO;
    }

    /**
     * Creates a new trade deal in the database.
     *
     * @param username     The username of the user creating the trade deal.
     * @param tradeDealDTO The TradeDealDTO representing the trade deal.
     * @return HTTP status code indicating the result of the operation.
     */
    public Integer createTradeDeal(String username, TradeDealDTO tradeDealDTO) {
        // Check if a trade deal with the same ID already exists
        if (isTradeDealIdExists(tradeDealDTO.getId())) {
            return 409; // HTTP status code for Conflict
        }

        // Check if the user owns the card and it is not locked in the deck
        if (!isCardOwnedByUser(username, tradeDealDTO.getCardToTrade()) || isCardLockedInDeck(username, tradeDealDTO.getCardToTrade())) {
            return 403; // HTTP status code for Forbidden
        }

        String sql = "INSERT INTO \"TradeDeal\" " +
                "(id, \"offeringUser_username\", \"offeredCard_id\", \"requirement_cardType\", \"requirement_minDamage\") " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(tradeDealDTO.getId()));
            preparedStatement.setString(2, username);
            preparedStatement.setObject(3, UUID.fromString(tradeDealDTO.getCardToTrade()));
            preparedStatement.setString(4, tradeDealDTO.getCardType());
            preparedStatement.setDouble(5, tradeDealDTO.getMinimumDamage());

            preparedStatement.executeUpdate();

            return 201; // HTTP status code for Created
        } catch (SQLException e) {
            e.printStackTrace();
            return 500; // HTTP status code for Internal Server Error
        }
    }

    /**
     * Checks if a trade deal with a given ID already exists in the database.
     *
     * @param tradeDealId The ID of the trade deal.
     * @return True if the trade deal exists, false otherwise.
     */
    private boolean isTradeDealIdExists(String tradeDealId) {
        String sql = "SELECT COUNT(*) FROM \"TradeDeal\" WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(tradeDealId));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Checks if a card is owned by a specific user.
     *
     * @param username The username of the user.
     * @param cardId   The ID of the card.
     * @return True if the user owns the card, false otherwise.
     */
    private boolean isCardOwnedByUser(String username, String cardId) {
        String sql = "SELECT COUNT(*) FROM \"Card\" WHERE id = ? AND owner_username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(cardId)); // Assuming it's a valid UUID
            preparedStatement.setString(2, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Checks if a card is locked in the user's deck.
     *
     * @param username The username of the user.
     * @param cardId   The ID of the card.
     * @return True if the card is locked in the deck, false otherwise.
     */
    private boolean isCardLockedInDeck(String username, String cardId) {
        String sql = "SELECT COUNT(*) FROM \"Deck\" WHERE username = ? AND (card1_id = ? OR card2_id = ? OR card3_id = ? OR card4_id = ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setObject(2, UUID.fromString(cardId));
            preparedStatement.setObject(3, UUID.fromString(cardId));
            preparedStatement.setObject(4, UUID.fromString(cardId));
            preparedStatement.setObject(5, UUID.fromString(cardId));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Deletes a trade deal from the database.
     *
     * @param username    The username of the user deleting the trade deal.
     * @param tradeDealId The ID of the trade deal.
     * @return HTTP status code indicating the result of the operation.
     */
    public Integer deleteTradeDeal(String username, String tradeDealId) {
        // Check if the trade deal exists
        if (!isTradeDealIdExists(tradeDealId)) {
            return 404; // HTTP status code for Not Found
        }

        // Check if the user owns the card associated with the trade deal
        String cardId = getCardIdFromTradeDeal(tradeDealId);
        if (!isCardOwnedByUser(username, cardId)) {
            return 403; // HTTP status code for Forbidden
        }

        String sql = "DELETE FROM \"TradeDeal\" WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(tradeDealId));

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return 200; // HTTP status code for OK
            } else {
                return 500; // HTTP status code for Internal Server Error
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 500; // HTTP status code for Internal Server Error
        }
    }

    /**
     * Retrieves the card ID associated with a trade deal.
     *
     * @param tradeDealId The ID of the trade deal.
     * @return The ID of the card associated with the trade deal.
     */
    private String getCardIdFromTradeDeal(String tradeDealId) {
        String sql = "SELECT \"offeredCard_id\" FROM \"TradeDeal\" WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(tradeDealId));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getObject(1, UUID.class).toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Carries out a trade, updating the trade deal status and transferring cards between users.
     *
     * @param username      The username of the user initiating the trade.
     * @param tradeDealId   The ID of the trade deal.
     * @param offeredCardId The ID of the card offered in the trade.
     * @return HTTP status code indicating the result of the operation.
     */
    public Integer carryOutTrade(String username, String tradeDealId, String offeredCardId) {
        // Check if the trade deal exists
        if (!isTradeDealIdExists(tradeDealId)) {
            return 404; // HTTP status code for Not Found
        }

        // Check if the user owns the trade deal
        if (isTradeDealBelongsToUser(username, tradeDealId)) {
            return 409; // HTTP status code for Forbidden
        }

        // Check if the user owns the offered card
        if (!isCardOwnedByUser(username, offeredCardId)) {
            return 403; // HTTP status code for Forbidden
        }

        // Check if the offered card meets the trade deal requirements
        if (!doesOfferedCardMeetRequirements(tradeDealId, offeredCardId)) {
            return 403; // HTTP status code for Forbidden
        }

        // Check if the offered card is locked in the user's deck
        if (isCardLockedInDeck(username, offeredCardId)) {
            return 403; // HTTP status code for Forbidden
        }

        // Carry out the trade
        String sql = "UPDATE \"TradeDeal\" SET status = 'COMPLETED' WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(tradeDealId));

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {

                System.out.println("accepting user tradedeal:");
                System.out.println(getOfferingUserUsername(tradeDealId));
                System.out.println("accepting cardid tradedeal:");
                System.out.println(getOfferedCardId(tradeDealId));

                System.out.println("offering user:");
                System.out.println(username);
                System.out.println("offered cardid:");
                System.out.println(offeredCardId);

                // Add cards to the user's stacks
                updateCardInUserStack(getOfferingUserUsername(tradeDealId), offeredCardId);
                updateCardInUserStack(username, getOfferedCardId(tradeDealId));

                // Update the owner_username attribute in the card table
                updateCardOwner(getOfferedCardId(tradeDealId), username);
                updateCardOwner(offeredCardId, getOfferingUserUsername(tradeDealId));

                // Delete the trade deal after successful trade
                if (!deleteDeal(tradeDealId)) {
                    // Handle the case where the trade deal couldn't be deleted
                    return 500; // HTTP status code for Internal Server Error
                }

                return 200; // HTTP status code for OK
            } else {
                return 500; // HTTP status code for Internal Server Error
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 500; // HTTP status code for Internal Server Error
        }
    }

    /**
     * Checks if the given trade deal belongs to the user carrying out the current deal.
     *
     * @param username     The username of the user carrying out the current trade deal.
     * @param tradeDealId  The ID of the trade deal to check.
     * @return True if the trade deal belongs to the user, false otherwise.
     */
    private boolean isTradeDealBelongsToUser(String username, String tradeDealId) {
        String sql = "SELECT \"offeringUser_username\" FROM \"TradeDeal\" WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(tradeDealId));

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String offeringUser = resultSet.getString("offeringUser_username");

                // Check if the offering user is the same as the user carrying out the trade deal
                return offeringUser.equals(username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Checks if the offered card meets the requirements specified in the trade deal.
     *
     * @param tradeDealId   The ID of the trade deal.
     * @param offeredCardId The ID of the card offered in the trade.
     * @return True if the offered card meets the requirements, false otherwise.
     */
    private boolean doesOfferedCardMeetRequirements(String tradeDealId, String offeredCardId) {
        String sql = "SELECT \"requirement_cardType\", \"requirement_minDamage\" FROM \"TradeDeal\" " +
                "WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(tradeDealId));

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String requiredCardType = resultSet.getString("requirement_cardType");
                Double requiredMinDamage = resultSet.getDouble("requirement_minDamage");

                String offeredCardType = getCardTypeFromCardId(offeredCardId);
                Double offeredDamage = getDamageFromCardId(offeredCardId);

                // Check if the offered card meets the requirements
                return offeredCardType.equalsIgnoreCase(requiredCardType) && offeredDamage >= requiredMinDamage;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates the owner of a card in the database.
     *
     * @param cardId           The ID of the card.
     * @param newOwnerUsername The username of the new owner.
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
     * Adds a purchased card to the user's stack in the database.
     *
     * @param username The username of the user.
     * @param cardId   The ID of the card to be added.
     * @throws SQLException If a database access error occurs.
     */
    public void updateCardInUserStack(String username, String cardId) throws SQLException {

        String insertQuery = "UPDATE \"Stack\" SET username = ? WHERE card_id = ?";

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
     * Deletes a trade deal from the "TradeDeal" table.
     *
     * @param tradeDealId The ID of the trade deal to be deleted.
     * @return True if the trade deal is successfully deleted, false otherwise.
     */
    private boolean deleteDeal(String tradeDealId) {
        String deleteSql = "DELETE FROM \"TradeDeal\" WHERE id = ?";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
            deleteStatement.setObject(1, UUID.fromString(tradeDealId));

            int rowsAffected = deleteStatement.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Helper method to get the card type from the card ID.
     *
     * @param cardId The ID of the card.
     * @return The card type.
     */
    private String getCardTypeFromCardId(String cardId) {
        String sql = "SELECT \"cardType\" FROM \"Card\" WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(cardId));
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getString("cardType") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to get the damage value from the card ID.
     *
     * @param cardId The ID of the card.
     * @return The damage value.
     */
    private double getDamageFromCardId(String cardId) {
        String sql = "SELECT damage FROM \"Card\" WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(cardId));
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getDouble("damage") : 0.0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Helper method to get the username of the user offering a trade deal.
     *
     * @param tradeDealId The ID of the trade deal.
     * @return The username of the user offering the trade deal.
     * @throws SQLException If a SQL exception occurs.
     */
    private String getOfferingUserUsername(String tradeDealId) throws SQLException {
        String sql = "SELECT \"offeringUser_username\" FROM \"TradeDeal\" WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(tradeDealId));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("offeringUser_username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper error handling
        }

        return null;
    }

    /**
     * Retrieves the ID of the card offered in a specific trade deal from the database.
     *
     * @param tradeDealId The ID of the trade deal.
     * @return A String representing the ID of the offered card or null if not found.
     * @throws SQLException If a database access error occurs.
     */
    private String getOfferedCardId(String tradeDealId) throws SQLException {
        String sql = "SELECT \"offeredCard_id\" FROM \"TradeDeal\" WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, UUID.fromString(tradeDealId));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getObject("offeredCard_id", UUID.class).toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw the exception to ensure proper error handling
        }

        return null;
    }

}

