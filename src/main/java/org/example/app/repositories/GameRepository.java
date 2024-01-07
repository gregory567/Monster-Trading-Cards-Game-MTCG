package org.example.app.repositories;

import org.example.app.daos.GameDAO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class GameRepository {
    private GameDAO gameDAO;

    public GameRepository(GameDAO gameDAO) {
        setGameDAO(gameDAO);
    }

    public String carryOutBattle(String username1, String username2) {
        return gameDAO.carryOutBattle(username1, username2);
    }
}
