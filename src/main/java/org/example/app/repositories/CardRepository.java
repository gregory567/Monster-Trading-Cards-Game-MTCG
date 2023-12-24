
package org.example.app.repositories;

import org.example.app.daos.CardDAO;
import org.example.app.dtos.CardDTO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class CardRepository {

    private CardDAO cardDAO;

    public CardRepository(CardDAO cardDAO) {
        setCardDAO(cardDAO);
    }


    public List<CardDTO> getCards(String username) {
        return cardDAO.getUserCards(username);
    }

    public List<CardDTO> getDeck(String username) {
        return cardDAO.getDeckCards(username);
    }

    public Integer updateDeck(String username, List<String> cardIds) {
        return cardDAO.updateUserDeck(username, cardIds);
    }

    public Integer createPackage(List<CardDTO> cards) {
        return cardDAO.createPackage(cards);
    }

    // Implement the buyPackage method
    public List<CardDTO> buyPackage(String username) throws InsufficientFundsException, CardPackageNotFoundException {
        // Implement the logic for buying a card package
        // You may need to interact with other components or services

        // For illustration purposes, let's assume you have a method in cardDAO for buying a package
        // Adjust the method signature and logic based on your actual implementation
        List<CardDTO> purchasedCards = cardDAO.buyPackage(username);

        // You should check if the purchasedCards list is empty, indicating that no package was found
        if (purchasedCards.isEmpty()) {
            throw new CardPackageNotFoundException("No card package available for buying");
        }

        // Check if the user has enough funds (adjust the logic based on your requirements)
        if (userHasSufficientFunds(username, purchasedCards)) {
            // Deduct the funds from the user's account (adjust the logic based on your requirements)
            deductFundsFromUser(username, purchasedCards);

            // Return the purchased cards
            return purchasedCards;
        } else {
            throw new InsufficientFundsException("Not enough money for buying a card package");
        }
    }

    // Example method to check if the user has sufficient funds
    private boolean userHasSufficientFunds(String username, List<CardDTO> purchasedCards) {
        // Implement the logic to check if the user has sufficient funds
        // You may interact with other components or services
        // Adjust the logic based on your actual requirements
        return true; // Placeholder, replace with actual logic
    }

    // Example method to deduct funds from the user
    private void deductFundsFromUser(String username, List<CardDTO> purchasedCards) {
        // Implement the logic to deduct funds from the user
        // You may interact with other components or services
        // Adjust the logic based on your actual requirements
    }

    public CardDTO get(String cardId) {
        return cardDAO.read(cardId);
    }

    public Integer add(CardDTO cardDTO) {
        return cardDAO.create(cardDTO);
    }

    public void remove(String cardId) {
        cardDAO.delete(cardId);
    }
}

