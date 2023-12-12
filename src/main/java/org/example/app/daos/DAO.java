package org.example.app.daos;

import java.util.ArrayList;

public interface DAO<T> {
    void create(String username, String password, Integer coins, String profile_name, String profile_email, String profile_other_details);
    ArrayList<T> readAll();
    T read(String username);
    void updateUsername(String oldUsername, String newUsername);
    void updatePassword(String username, String newPassword);
    void updateCoins(String username, Integer newCoins);
    void updateProfileName(String username, String newProfileName);
    void updateProfileEmail(String username, String newProfileEmail);
    void updateProfileOtherDetails(String username, String newProfileOtherDetails);
    void updateEloScore(String username, Integer newEloScore);
    void delete(String username);
}
