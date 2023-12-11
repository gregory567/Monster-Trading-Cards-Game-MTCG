package org.example.app.repositories;

import org.example.app.daos.UserDAO;
import org.example.app.models.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class UserRepository implements Repository<User> {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    UserDAO userDAO;

    public UserRepository(UserDAO userDAO) {
        setUserDAO(userDAO);
    }

    @Override
    public ArrayList<User> getAll() {
        ArrayList<User> users = getUserDAO().readAll();

        return users;
    }

    @Override
    public User get(int id) {
        return null;
    }

    @Override
    public void add(User user) {
        getUserDAO().create(user);
    }

    @Override
    public void update(User type) {

    }

    @Override
    public void remove(User type) {

    }
}
