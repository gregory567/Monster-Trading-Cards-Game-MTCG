package org.example;

import lombok.Getter;
import lombok.Setter;

// wenn man für alle variablen getter und setter haben möchte
/*@Getter
@Setter*/
public class User {
    // wenn man für einzelne variablen getter und setter haben möchte 
    @Getter
    @Setter
    private String username;
    private String password;
    private double coins;
    public void tradeCard(){

    }
    public void buyPackage(Package newPackage){

    }
    public void selectBestCards(){

    }
    public void register(){

    }
    public void login(){

    }
    public void viewScores(){

    }

}
