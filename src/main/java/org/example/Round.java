package org.example;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.example.app.models.User;

@Getter
@Setter
public class Round {
    private List<Card> cardsPlayed;
    private Integer roundNumber;
    private User winner;
    private boolean draw;

    public Round(List<Card> cardsPlayed, Integer roundNumber) {
        this.cardsPlayed = cardsPlayed;
        this.roundNumber = roundNumber;
    }

    public void determineRoundOutcome() {
        // Implement logic to determine the outcome of the round
        Card card1 = cardsPlayed.get(0);
        Card card2 = cardsPlayed.get(1);

        int result = compareCards(card1, card2);

        if (result > 0) {
            winner = card1.getOwner();
        } else if (result < 0) {
            winner = card2.getOwner();
        } else {
            draw = true;
        }
    }

    private int compareCards(Card card1, Card card2) {
        // Implement logic to compare cards and determine the winner
        // This could be based on damage, element type, etc.
        // Return a positive number if card1 wins, a negative number if card2 wins, and 0 for a draw.
        // For simplicity, let's compare based on damage.
        return Integer.compare(card1.getDamage(), card2.getDamage());
    }

    public String getRoundOutcome() {
        // Return the outcome of the round as a string
        if (draw) {
            return "Draw";
        } else {
            return "Winner: " + winner.getUsername();  // assuming getName() is a method in the User class
        }
    }
    
}

