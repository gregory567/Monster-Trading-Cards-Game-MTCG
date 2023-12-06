package org.example;

import org.example.Specialty;
import org.example.ElementType;
import org.example.CardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Card {
    protected String name;
    protected Integer damage;
    protected String elementType;
    protected Specialty[] specialties;
    protected CardType cardType;

    public Card(String name, Integer damage, String elementType, Specialty[] specialties) {
    }

    public abstract void getAttributes();

    public abstract void displayCardInfo();

    public abstract void upgradeCard();

    public abstract void calculateEffectiveDamage(ElementType opponentElementType, CardType opponentCardType);

    public void applySpecialty(Specialty specialty) {
        if(specialties != null) {
            for (Specialty cardSpecialty : specialties) {
                if (cardSpecialty.getName().equals(specialty.getName())) {
                    cardSpecialty.applySpecialtyEffect(this);
                    // You might want to break here if each card can have only one instance of a specialty
                }
            }
        }
    }

}
