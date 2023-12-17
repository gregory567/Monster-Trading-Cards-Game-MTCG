package org.example.app.services;

import org.example.Deck;
import org.example.Stack;
import org.example.Profile;
import org.example.app.models.User;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class UserService {

    private List<User> userData;

    public UserService() {
        setUserData(new ArrayList<>());
        //userData.add(new User("user1", "password1", 100.0, new Stack(), new Deck(), null));
        //userData.add(new User("user2", "password2", 150.0, new Stack(), new Deck(), null));
        //userData.add(new User("user3", "password3", 200.0, new Stack(), new Deck(), null));
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

    /*
    public User createUser(String username, String password, double coins, Stack stack, Deck deck, Profile profile) {
        User newUser = new User(username, password, coins, stack, deck, profile);
        userData.add(newUser);
        return newUser;
    }
     */

    public void removeUser(String username) {
        userData.removeIf(user -> Objects.equals(username, user.getUsername()));
    }
}
