package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import org.example.Deck;
import org.example.Package;
import org.example.Profile;
import org.example.Stack;

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

    // Jackson needs the default constructor
    public User() {}

    public void tradeCard() {

    }

    public void buyPackage(Package newPackage) {

    }

    public void selectBestCards() {

    }

    public void register() {

    }

    public void login() {

    }

    public void viewScores() {

    }

    public void viewProfile() {

    }

    public void editProfile() {

    }
}
