package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import org.example.Deck;
import org.example.Package;
import org.example.Profile;
import org.example.Stack;
import org.example.Card;
import org.example.MonsterCard;
import org.example.SpellCard;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

// wenn man für alle variablen getter und setter haben möchte
/*@Getter
@Setter*/
@Getter
@Setter
public class User {
    // wenn man für einzelne variablen getter und setter haben möchte 
    //@Getter
    //@Setter
    @JsonAlias({"username"})
    private String username;
    @JsonAlias({"password"})
    private String password;
    @JsonAlias({"coins"})
    private double coins;
    @JsonAlias({"stack"})
    private Stack stack;
    @JsonAlias({"deck"})
    private Deck deck;
    @JsonAlias({"profile"})
    private Profile profile;
    private BattleResult[] battleResults;

    // Jackson needs the default constructor
    public User() {}

    public void tradeCard(Card card, Requirement requirement) {
        if (requirement.satisfiesRequirement(card)) {
            // remove card from user's stack
            stack.removeCard(card);

            // add the traded card to the user's stack
            stack.attainCard(card);

            // log the trade or perform any other necessary actions

        } else {
            // handle case where the card doesn't meet the trading requirements
            System.out.println("The card does not meet the trading requirements.");
        }
    }

    public void buyPackage(Package newPackage) {
        if (coins >= 5) {
            // deduct the cost of the package from user's coins
            coins -= 5;

            // open the package and add the cards to the user's stack
            newPackage.openPackage();
            for (Card card : newPackage.getPackageCards()) {
                stack.attainCard(card);
            }

            // log the purchase or perform any other necessary actions

        } else {
            // handle case where the user does not have enough coins to buy the package
            System.out.println("Not enough coins to buy the package.");
        }
    }

    public void selectBestCards() {
        List<Card> allMonsterCards = new ArrayList<>();
        List<Card> allSpellCards = new ArrayList<>();

        // Separate monster cards and spell cards from the user's stack
        for (Card card : stack.getStackCards()) {
            if (card instanceof MonsterCard) {
                allMonsterCards.add(card);
            } else if (card instanceof SpellCard) {
                allSpellCards.add(card);
            }
        }

        // Sort monster cards and spell cards by damage in descending order
        Comparator<Card> damageComparator = Comparator.comparing(Card::getDamage).reversed();
        allMonsterCards.sort(damageComparator);
        allSpellCards.sort(damageComparator);

        // Select the top 2 monster cards and the top 2 spell cards
        List<Card> selectedMonsterCards = allMonsterCards.stream().limit(2).collect(Collectors.toList());
        List<Card> selectedSpellCards = allSpellCards.stream().limit(2).collect(Collectors.toList());

        // Clear and update the user's deck with the selected cards
        deck.getBestCards().clear();
        deck.getBestCards().addAll(selectedMonsterCards);
        deck.getBestCards().addAll(selectedSpellCards);
    }

    public void register() {
        // Implement logic for user registration
        // You might want to perform validation, store user data, etc.
    }

    public void login() {
        // Implement logic for user login
        // You might want to authenticate the user, generate a session token, etc.
    }

    public void viewScores() {
        // Implement logic to view user scores
        // You might want to retrieve and display the user's scores or rankings
    }

    public void viewProfile() {
        // Implement logic to view user profile
        // You can display user profile information such as name, email, etc.
    }

    public void editProfile() {
        // Implement logic to edit user profile
        // You might want to allow the user to update their profile information
    }
}
