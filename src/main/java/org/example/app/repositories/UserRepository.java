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

    public List<UserDataDTO> getUsers() {
        return userDAO.getUsers();
    }

    public UserDataDTO getUser(String username) {
        return userDAO.getUser(username);
    }

    public Integer createUser(String username, String password) {
        return userDAO.createUser(username, password);
    }

    public Integer updateUser(String username, String name, String bio, String image) {
        return userDAO.updateUser(username, name, bio, image);
    }

    public String loginUser(String username, String password) {
        return userDAO.loginUser(username, password);
    }

    public String logoutUser(String username) {
        return userDAO.logoutUser(username);
    }

    public UserStatDTO getStats(String username) {
        return userDAO.getStats(username);
    }

    public List<UserStatDTO> getScoreBoard() {
        return userDAO.getScoreBoard();
    }

    public void deleteUser(String username) {
        userDAO.deleteUser(username);
    }
}
