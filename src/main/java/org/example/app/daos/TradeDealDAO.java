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



}

