
package org.example.app.repositories;

import org.example.TradeDeal;
import org.example.app.daos.TradeDealDAO;
import org.example.app.dtos.TradeDealDTO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class TradeDealRepository {

    private TradeDealDAO tradeDealDAO;

    public TradeDealRepository(TradeDealDAO tradeDealDAO) {
        setTradeDealDAO(tradeDealDAO);
    }

    public List<TradeDealDTO> getTradeDeals() {
        return tradeDealDAO.getTradeDeals();
    }

    public Integer createTradeDeal(String username, TradeDealDTO tradeDealDTO) {
        return tradeDealDAO.createTradeDeal(username, tradeDealDTO);
    }

    public Integer deleteTradeDeal(String username, String tradeDealId) {
        return tradeDealDAO.deleteTradeDeal(username, tradeDealId);
    }

}

