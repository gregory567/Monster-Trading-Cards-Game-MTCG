package org.example;

import org.example.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    private List<User> users;
    private List<Battle> battles;

    public GameController() {
        this.users = new ArrayList<>();
        this.battles = new ArrayList<>();
    }

    public void createUser(String username, String password, double coins, Stack stack, Deck deck, Profile profile) {
        // Implement the logic to create a new user
        User newUser = new User(username, password, coins, stack, deck, profile);
        users.add(newUser);
    }

    public void initiateBattle(User user1, User user2) {
        // Implement the logic to initiate a battle between two users
        Battle newBattle = new Battle(user1, user2);
        battles.add(newBattle);
        newBattle.startBattle();
    }
}
