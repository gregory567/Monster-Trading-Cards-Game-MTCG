package org.example;

import org.example.app.models.User;
import org.example.BattleLog;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Battle {
    private User user1;
    private User user2;
    private BattleLog battleLog;

    public Battle(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.battleLog = new BattleLog();
    }

    public void startBattle() {
        // Logic to start the battle and determine the outcome of each round
        for (int roundNumber = 1; roundNumber <= 100; roundNumber++) {
            // Get cards played by each user for the current round
            Card card1 = user1.getDeck().getBestCards().get(roundNumber - 1);
            Card card2 = user2.getDeck().getBestCards().get(roundNumber - 1);

            List cardsPlayed = new List<Card> (card1, card2);
            // Create a Round object
            Round round = new Round(cardsPlayed);

            // Determine the outcome of the round
            round.determineRoundOutcome();

            // Log the round
            battleLog.logRound(round);

            // If the battle is over, end the battle
            if (battleLog.getOutcome() != null) {
                endBattle();
                break;
            }
        }
    }

    public void endBattle() {
        // Logic to end the battle, update player stats, and display results
        displayBattleResults();
        updatePlayerStats();
    }

    public void displayBattleResults() {
        // Logic to display battle results, including the detailed battle log
        System.out.println("Battle Results:");
        System.out.println("Outcome: " + battleLog.getOutcome());
        System.out.println("Detailed Battle Log:");
        for (Round round : battleLog.getRounds()) {
            System.out.println("Round " + round.getRoundNumber() + ": " + round.getOutcome());
        }
    }

    public void updatePlayerStats() {
        // Logic to update player stats based on the battle outcome
        BattleResult result1 = new BattleResult(user2, battleLog.getOutcome());
        BattleResult result2 = new BattleResult(user1, reverseOutcome(battleLog.getOutcome()));

        user1.getBattleResults().add(result1);
        user2.getBattleResults().add(result2);
    }

    private String reverseOutcome(String outcome) {
        // Helper method to reverse the outcome for the opponent
        if ("win".equals(outcome)) {
            return "loss";
        } else if ("loss".equals(outcome)) {
            return "win";
        } else {
            return "draw";
        }
    }
}


