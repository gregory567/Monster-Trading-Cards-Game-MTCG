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
import org.example.BattleResult;
import org.example.Requirement;
import org.example.TradeDeal;
import org.example.Store;

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
    /*@Getter
    @Setter*/
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
    @JsonAlias({"eloscore"})
    private Integer eloScore;
    private BattleResult[] battleResults;
    private List<TradeDeal> initiatedTrades;
    private List<TradeDeal> acceptedTrades;

    public User() {
        this.initiatedTrades = new ArrayList<>();
        this.acceptedTrades = new ArrayList<>();
    }

    public User(String username, String password, double coins, Stack stack, Deck deck, Profile profile, Integer eloScore, BattleResult[] battleResults, List<TradeDeal> initiatedTrades, List<TradeDeal> acceptedTrades) {
        this.username = username;
        this.password = password;
        this.coins = coins;

        // Initialize stack and deck with empty instances
        this.stack = (stack != null) ? stack : new Stack();
        this.deck = (deck != null) ? deck : new Deck();

        this.profile = profile;
        this.eloScore = eloScore;

        // Check if battleResults, initiatedTrades, and acceptedTrades are null or empty
        this.battleResults = (battleResults != null && battleResults.length > 0) ? battleResults : new BattleResult[0];
        this.initiatedTrades = (initiatedTrades != null && !initiatedTrades.isEmpty()) ? initiatedTrades : new ArrayList<>();
        this.acceptedTrades = (acceptedTrades != null && !acceptedTrades.isEmpty()) ? acceptedTrades : new ArrayList<>();
    }

    public void requestTrade(Card card, Requirement requirement) {
        TradeDeal tradeDeal = new TradeDeal(this, card, requirement);
        Store.addTradeDeal(tradeDeal);
        initiatedTrades.add(tradeDeal);
    }

    public void acceptTrade(TradeDeal tradeDeal) {
        tradeDeal.acceptDeal(this);
        acceptedTrades.add(tradeDeal);
    }

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
        System.out.println("User registered successfully!");
    }

    public void login() {
        // Implement logic for user login
        // You might want to authenticate the user, generate a session token, etc.
        System.out.println("User logged in successfully!");
    }

    public void viewScores() {
        // Implement logic to view user scores
        // You might want to retrieve and display the user's scores or rankings
        System.out.println("Viewing user scores...");
    }

    public void viewProfile() {
        // Implement logic to view user profile
        // You can display user profile information such as name, email, etc.
        if (profile != null) {
            System.out.println("User Profile:");
            System.out.println("Name: " + profile.getName());
            System.out.println("Email: " + profile.getEmail());
            System.out.println("Other Details: " + profile.getOtherDetails());
        } else {
            System.out.println("User profile not available.");
        }
    }

    public void editProfile() {
        // Implement logic to edit user profile
        // You might want to allow the user to update their profile information
        if (profile != null) {
            System.out.println("Editing user profile...");
            // You can add logic here to update profile details
            // For example, take input from the user to update name, email, etc.
            // profile.setName(updatedName);
            // profile.setEmail(updatedEmail);
            // profile.setOtherDetails(updatedOtherDetails);
            System.out.println("User profile updated successfully!");
        } else {
            System.out.println("User profile not available for editing.");
        }
    }

}
