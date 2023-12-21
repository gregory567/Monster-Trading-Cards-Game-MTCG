
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

