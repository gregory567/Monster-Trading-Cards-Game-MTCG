package org.example.app.daos;

import org.example.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.dtos.TradeDealDTO;
import org.example.app.repositories.TradeDealRepository;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class TradeDealDAO {

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    @Setter(AccessLevel.PRIVATE)
    ArrayList<TradeDealDTO> tradeDealCache;

    public TradeDealDAO(Connection connection) {
        setConnection(connection);
    }

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

    private TradeDealDTO mapResultSetToTradeDealDTO(ResultSet resultSet) throws SQLException {
        TradeDealDTO tradeDealDTO = new TradeDealDTO();
        tradeDealDTO.setId(resultSet.getString("id"));
        tradeDealDTO.setCardToTrade(resultSet.getString("offeredCard_id"));
        tradeDealDTO.setCardType(resultSet.getString("requirement_cardType"));
        tradeDealDTO.setMinimumDamage(resultSet.getDouble("requirement_minDamage"));
        return tradeDealDTO;
    }

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
                "(id, offeringUser_username, offeredCard_id, requirement_cardType, requirement_minDamage) " +
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

    private boolean isCardLockedInDeck(String username, String cardId) {
        String sql = "SELECT COUNT(*) FROM \"Deck\" WHERE username = ? AND (card1_id = ? OR card2_id = ? OR card3_id = ? OR card4_id = ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setObject(2, UUID.fromString(cardId)); // Assuming it's a valid UUID
            preparedStatement.setObject(3, UUID.fromString(cardId)); // Assuming it's a valid UUID
            preparedStatement.setObject(4, UUID.fromString(cardId)); // Assuming it's a valid UUID
            preparedStatement.setObject(5, UUID.fromString(cardId)); // Assuming it's a valid UUID
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

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

    private String getCardIdFromTradeDeal(String tradeDealId) {
        String sql = "SELECT offeredCard_id FROM \"TradeDeal\" WHERE id = ?";

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



}

