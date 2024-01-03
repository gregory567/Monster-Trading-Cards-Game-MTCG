package org.example;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.example.CardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Requirement {
    @JsonAlias({"CardType"})
    private CardType cardType;
    @JsonAlias({"MinimumDamage"})
    private Integer minDamage;

    public Requirement(CardType cardType, Integer minDamage) {
        this.cardType = cardType;
        this.minDamage = minDamage;
    }

    public boolean satisfiesRequirement(Card card) {
        // Check if the card type matches and damage is greater than or equal to the minimum required
        return card.getCardType() == cardType && card.getDamage() >= minDamage;
    }
}
