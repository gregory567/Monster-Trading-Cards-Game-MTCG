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

}
