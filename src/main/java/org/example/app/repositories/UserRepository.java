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
    public void add(String username, String password, Integer coins, String profile_name, String profile_email, String profile_other_details) {
        userDAO.create(username, password, coins, profile_name, profile_email, profile_other_details);
    }

    @Override
    public void updateUsername(String oldUsername, String newUsername) {
        userDAO.updateUsername(oldUsername, newUsername);
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        userDAO.updatePassword(username, newPassword);
    }

    @Override
    public void updateCoins(String username, Integer newCoins) {
        userDAO.updateCoins(username, newCoins);
    }

    @Override
    public void updateProfileName(String username, String newProfileName) {
        userDAO.updateProfileName(username, newProfileName);
    }

    @Override
    public void updateProfileEmail(String username, String newProfileEmail) {
        userDAO.updateProfileEmail(username, newProfileEmail);
    }

    @Override
    public void updateProfileOtherDetails(String username, String newProfileOtherDetails) {
        userDAO.updateProfileOtherDetails(username, newProfileOtherDetails);
    }

    @Override
    public void updateEloScore(String username, Integer newEloScore) {
        userDAO.updateEloScore(username, newEloScore);
    }

    @Override
    public void remove(String username) {
        userDAO.delete(username);
    }
}
