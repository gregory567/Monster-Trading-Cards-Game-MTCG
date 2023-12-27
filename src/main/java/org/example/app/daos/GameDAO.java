package org.example.app.daos;
import org.example.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.app.dtos.CardDTO;
import org.example.app.repositories.GameRepository;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class GameDAO {

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    public GameDAO(Connection connection) {
        setConnection(connection);
    }

    public String carryOutBattle(String username) {

    }

}
