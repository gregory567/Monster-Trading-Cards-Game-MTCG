package org.example;

import org.example.Card;
import org.example.app.models.User;
import org.example.Requirement;
import lombok.Setter;
import lombok.Getter;

@Setter
@Getter
public class TradeDeal {
    private User offeringUser;
    private Card offeredCard;
    private Requirement requirement;

    public TradeDeal(User offeringUser, Card offeredCard, Requirement requirement) {
        this.offeringUser = offeringUser;
        this.offeredCard = offeredCard;
        this.requirement = requirement;
    }

    public void acceptDeal(User acceptingUser) {
        if (requirement.satisfiesRequirement(offeredCard)) {
            // Perform the trade
            offeringUser.getStack().removeCard(offeredCard);
            acceptingUser.getStack().attainCard(offeredCard);

            // Log the trade or perform any other necessary actions

        } else {
            // Handle case where the card doesn't meet the trading requirements
            System.out.println("The card does not meet the trading requirements.");
        }
    }

    public void rejectDeal() {
        // Handle the rejection of the trade deal
        // This could involve logging or notifying the offering user
        System.out.println("Trade deal rejected.");
    }
}

