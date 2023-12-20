
package org.example.app.repositories;

import org.example.app.daos.CardDAO;
import org.example.app.dtos.CardDTO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class CardRepository implements Repository<CardDTO> {

    private CardDAO cardDAO;

    public CardRepository(CardDAO cardDAO) {
        setCardDAO(cardDAO);
    }

    @Override
    public List<CardDTO> getAll(String username) {
        return cardDAO.readAll(username);
    }

    @Override
    public CardDTO get(String cardId) {
        return cardDAO.read(cardId);
    }

    @Override
    public Integer add(CardDTO cardDTO) {
        return cardDAO.create(cardDTO);
    }

    @Override
    public Integer update(CardDTO cardDTO) {
        return cardDAO.update(cardDTO);
    }

    @Override
    public void remove(String cardId) {
        cardDAO.delete(cardId);
    }
}

