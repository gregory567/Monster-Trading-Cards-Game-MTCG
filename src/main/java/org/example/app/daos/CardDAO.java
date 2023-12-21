package org.example.app.daos;

import org.example.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.dtos.CardDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CardDAO {

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    @Setter(AccessLevel.PRIVATE)
    ArrayList<CardDTO> cardsCache;

    public CardDAO(Connection connection) {
        setConnection(connection);
    }

    public Integer create(CardDTO cardDTO) {
        // Implement card creation logic here
        // Return appropriate HTTP status codes based on the result
        return null;
    }

    public ArrayList<CardDTO> readAll(String username) {
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

    // Helper method to create a DTO from a ResultSet
    private CardDTO createCardDTOFromResultSet(ResultSet resultSet) throws SQLException {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(resultSet.getString("id"));
        cardDTO.setName(resultSet.getString("name"));
        cardDTO.setDamage(resultSet.getInt("damage"));

        return cardDTO;
    }


    public CardDTO read(String cardId) {
        // Implement logic to retrieve a card by its ID from the database
        // and return the Card object
        return null;
    }

    public Integer update(Card card) {
        // Implement logic to update the card in the database
        // Return appropriate HTTP status codes based on the result
        return null;
    }

    public void delete(String cardId) {
        // Implement logic to delete a card by its ID from the database
    }

    // Additional methods specific to the CardDAO

    public List<Card> getUserCards(String username) {
        // Implement logic to retrieve cards owned by the specified user
        // and return the list of cards
        return null;
    }

    public List<Card> getDeckCards(String username) {
        // Implement logic to retrieve cards in the user's deck
        // and return the list of cards
        return null;
    }

    public Integer configureDeck(String username, List<String> cardIds) {
        // Implement logic to configure the user's deck with the provided cards
        // Return appropriate HTTP status codes based on the result
        return null;
    }

    public List<TradingDeal> getAvailableTradingDeals() {
        // Implement logic to retrieve available trading deals
        // and return the list of trading deals
        return null;
    }

    public Integer createTradingDeal(TradingDeal tradingDeal) {
        // Implement logic to create a new trading deal in the database
        // Return appropriate HTTP status codes based on the result
        return null;
    }

    public Integer deleteTradingDeal(String tradingDealId) {
        // Implement logic to delete a trading deal by its ID from the database
        // Return appropriate HTTP status codes based on the result
        return null;
    }

    public Integer executeTrade(String tradingDealId, String offeredCardId) {
        // Implement logic to carry out a trade for the specified trading deal
        // Return appropriate HTTP status codes based on the result
        return null;
    }

    public UserStats getUserStats(String username) {
        // Implement logic to retrieve user stats from the database
        // and return the UserStats object
        return null;
    }

    public List<UserStats> getScoreboard() {
        // Implement logic to retrieve the user scoreboard from the database
        // and return the list of UserStats objects
        return null;
    }

    public String enterBattleLobby(String username) {
        // Implement logic to enter the battle lobby and start a battle if another user is present
        // Return the battle log or appropriate message
        return null;
    }
}

