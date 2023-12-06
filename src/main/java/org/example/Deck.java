package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Deck {
    private List<Card> bestCards;

    public Deck() {
        this.bestCards = new ArrayList<>();
    }

    public void addCardToDeck(Card card) {
        // check if the card is not already in the deck
        if (!bestCards.contains(card)) {
            // check if there is space in the deck
            if (bestCards.size() < 4) {
                // add the card to the deck
                bestCards.add(card);
            } else {
                // handle the case where the deck is full
                System.out.println("Deck is full. Cannot add more cards.");
            }
        } else {
            // handle the case where the card is already in the deck
            System.out.println("Card is already in the deck.");
        }
    }

    public void removeCardFromDeck(Card card) {
        // check if the card is in the deck
        if (bestCards.contains(card)) {
            // remove the card from the deck
            bestCards.remove(card);
        } else {
            // handle the case where the card is not in the deck
            System.out.println("Card not found in the deck.");
        }
    }

    public void reorganizeDeck() {
        // reorganize the deck by choosing the 4 cards with the highest damage
        bestCards.sort(Comparator.comparing(Card::getDamage).reversed());

        // keep only the top 4 cards
        if (bestCards.size() > 4) {
            bestCards = bestCards.subList(0, 4);
        }

        System.out.println("Deck reorganized.");
    }

}
