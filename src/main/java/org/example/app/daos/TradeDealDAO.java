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

}

