package org.example.app.repositories;

import org.example.app.daos.UserDAO;
import org.example.app.dtos.UserDataDTO;
import org.example.app.dtos.UserStatDTO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class UserRepository {

    private UserDAO userDAO;

    public UserRepository(UserDAO userDAO) {
        setUserDAO(userDAO);
    }

    public List<UserDataDTO> getAll() {
        return userDAO.readAll();
    }

    public UserDataDTO get(String username) {
        return userDAO.read(username);
    }

    public Integer add(String username, String password) {
        return userDAO.create(username, password);
    }

    public Integer updateUser(String username, String name, String bio, String image) {
        return userDAO.updateUser(username, name, bio, image);
    }

    public String loginUser(String username, String password) {
        return userDAO.loginUser(username, password);
    }

    public UserStatDTO getStats(String username) {
        return userDAO.getStats(username);
    }

    public List<UserStatDTO> getScoreBoard() {
        return userDAO.getScoreBoard();
    }

    public void remove(String username) {
        userDAO.delete(username);
    }
}
