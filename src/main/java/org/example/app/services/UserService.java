package org.example.app.services;

import org.example.Deck;
import org.example.Stack;
import org.example.app.models.User;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserService {
    @Setter(AccessLevel.PRIVATE)
    private List<User> userData;

    public UserService() {
        setUserData(new ArrayList<>());
        userData.add(new User());
        userData.add(new User());
        userData.add(new User());
    }

    public User getUserByUsername(String username) {
        User foundUser = userData.stream()
                .filter(user -> Objects.equals(username, user.getUsername()))
                .findAny()
                .orElse(null);

        return foundUser;
    }

    public List<User> getUsers() {
        return userData;
    }

    public void addUser(User user) {
        userData.add(user);
    }

    public void removeUser(String username) {
        userData.removeIf(user -> Objects.equals(username, user.getUsername()));
    }
}
