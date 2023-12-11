package org.example.app.daos;

import org.example.app.models.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserDAO implements DAO<User> {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    @Setter(AccessLevel.PRIVATE)
    ArrayList<User> usersCache;

    public UserDAO(Connection connection) {
        setConnection(connection);
    }

    @Override
    public void create(User user) {
        String insertStmt = "INSERT into users (name, population) VALUES (?, ?);";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(insertStmt);
            preparedStatement.setString(1, city.getName());
            preparedStatement.setInt(2, city.getPopulation());
            preparedStatement.execute();
            getConnection().close();
            setUsersCache(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<User> readAll() {
        ArrayList<User> users = new ArrayList();

        if (usersCache != null) {
            System.out.println("TEST");
            return usersCache;
        }

        String insertStmt = "SELECT name, population from users;";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(insertStmt);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                User user = new User(resultSet.getString(1), resultSet.getInt(2));
                users.add(user);
            }
            setUsersCache(users);
            getConnection().close();
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public User read(int id) {
        return null;
    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(int id) {

    }
}


