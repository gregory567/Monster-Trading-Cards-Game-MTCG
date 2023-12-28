package org.example;

import org.example.app.models.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Card {

    protected CardName name;
    protected Integer damage;
    protected ElementType elementType;
    protected String[] specialties;
    protected CardType cardType;
    protected String owner;

    public Card(CardName name, Integer damage, ElementType elementType, String[] specialties, String owner) {
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.specialties = specialties;
        this.owner = owner;
    }

    public abstract void getAttributes();

    public abstract void displayCardInfo();

    public void upgradeCard(int upgradeAmount) {
        // increase the damage of the card when upgraded
        int upgradedDamage = getDamage() + upgradeAmount;
        setDamage(upgradedDamage);

        System.out.println(getCardType() + " upgraded! New damage: " + upgradedDamage);
    }

    public abstract Integer calculateEffectiveDamage(Card opponentCard);

    protected String specialtiesToString() {
        if (getSpecialties() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String specialty : getSpecialties()) {
                stringBuilder.append(specialty).append(", ");
            }
            // remove the trailing comma and space
            return stringBuilder.substring(0, stringBuilder.length() - 2);
        }
        return "None";
    }

}
