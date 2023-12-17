package org.example.app.repositories;

import org.example.app.daos.UserDAO;
import org.example.app.models.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class UserRepository implements Repository<User> {

    private UserDAO userDAO;

    public UserRepository(UserDAO userDAO) {
        setUserDAO(userDAO);
    }

    @Override
    public List<User> getAll() {
        return userDAO.readAll();
    }

    @Override
    public User get(String username) {
        return userDAO.read(username);
    }

    @Override
    public Integer add(String username, String password) {
        return userDAO.create(username, password);
    }

    @Override
    public Integer updateUser(String username, String name, String bio, String image) {
        return userDAO.updateUser(username, name, bio, image);
    }

    @Override
    public String loginUser(String username, String password) {
        return userDAO.loginUser(username, password);
    }

    @Override
    public void remove(String username) {
        userDAO.delete(username);
    }
}
