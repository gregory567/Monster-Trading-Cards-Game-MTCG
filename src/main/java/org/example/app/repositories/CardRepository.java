
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

    // Define InsufficientFundsException as a nested static class
    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    // Define CardPackageNotFoundException as a nested static class
    public static class CardPackageNotFoundException extends Exception {
        public CardPackageNotFoundException(String message) {
            super(message);
        }
    }

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

    public List<CardDTO> buyPackage(String username) throws InsufficientFundsException, CardPackageNotFoundException {
        return cardDAO.buyPackage(username);
    }

    public CardDTO get(String cardId) {
        return cardDAO.read(cardId);
    }

}

