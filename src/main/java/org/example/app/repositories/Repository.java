package org.example.app.repositories;

import java.util.List;

public interface Repository<T> {
    List<T> getAll();
    T get(String username);
    void add(String username, String password, Integer coins, String profile_name, String profile_email, String profile_other_details);
    void updateUsername(String oldUsername, String newUsername);
    void updatePassword(String username, String newPassword);
    void updateCoins(String username, Integer newCoins);
    void updateProfileName(String username, String newProfileName);
    void updateProfileEmail(String username, String newProfileEmail);
    void updateProfileOtherDetails(String username, String newProfileOtherDetails);
    void updateEloScore(String username, Integer newEloScore);
    void remove(String username);
}
