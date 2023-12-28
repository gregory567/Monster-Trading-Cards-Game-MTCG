package org.example;

import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import org.example.ElementType;
import org.example.CardName;


@Getter
@Setter
public class Package {

    // array to store the cards in the package
    private Card[] packageCards;

    // constructor initializes the package with an array of 5 cards
    public Package() {
        this.packageCards = new Card[5];
    }

    // opens the package and generates random MonsterCard or SpellCard instances
    public void openPackage() {
        for (int i = 0; i < packageCards.length; i++) {
            // random boolean to decide whether to create a MonsterCard or SpellCard
            if (new Random().nextBoolean()) {
                // create a new instance of MonsterCard with random attributes
                packageCards[i] = new MonsterCard(generateRandomName(), generateRandomDamage(), generateRandomElementType(), new String[]{String.valueOf(generateRandomName())}, null);
            } else {
                // create a new instance of SpellCard with random attributes
                packageCards[i] = new SpellCard(generateRandomName(), generateRandomDamage(), generateRandomElementType(), new String[]{String.valueOf(generateRandomName())}, null);
            }
        }
    }

    // display information about the cards in the package
    public void displayPackageInfo() {
        System.out.println("Package Information:");
        for (Card card : packageCards) {
            card.displayCardInfo();
        }
    }

    // apply a discount to the cards in the package
    public void applyDiscount() {
        // Implement logic to apply a discount to the packageCards
        // You may want to reduce the damage, change specialties, or apply other effects
    }

    // generate a random name for a card from the CardName enum
    private CardName generateRandomName() {
        CardName[] cardNames = CardName.values();
        return cardNames[new Random().nextInt(cardNames.length)];
    }

    // generate a random damage value for a card
    private int generateRandomDamage() {
        return new Random().nextInt(100) + 1; // random value between 1 and 100
    }

    // generate a random ElementType for a card
    private ElementType generateRandomElementType() {
        ElementType[] elementTypes = ElementType.values();
        return elementTypes[new Random().nextInt(elementTypes.length)];
    }
}
