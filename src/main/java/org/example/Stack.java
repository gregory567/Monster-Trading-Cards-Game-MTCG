package org.example;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.example.ElementType;

@Getter
@Setter
public class Stack {
    private List<Card> stackCards;

    public Stack() {
        this.stackCards = new ArrayList<>();
    }

    public void removeCard(Card card) {
        stackCards.remove(card);
    }

    public void attainCard(Card card) {
        stackCards.add(card);
    }

    public void shuffleCards() {
        Collections.shuffle(stackCards);
    }

    //Organizes the cards in the stack by their element type in the order: Fire, Water, Normal.
    public void organizeByElement() {
        // use Comparator.comparing to create a comparator based on the ordinal values of the ElementType enum
        stackCards.sort(Comparator.comparing(card -> {
            // convert the string representation of the element type to the corresponding enum value
            ElementType elementType = ElementType.valueOf(card.getElementType());
            // return the ordinal value of the enum, which corresponds to the order in which the enum constants are declared
            return elementType.ordinal();
        }));
    }

}
