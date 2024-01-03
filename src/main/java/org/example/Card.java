package org.example;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public abstract class Card {

    protected UUID Id;
    protected CardName name;
    protected Double damage;
    protected ElementType elementType;
    protected String[] specialties;
    protected CardType cardType;
    protected String ownerUsername;

    public Card(UUID Id, CardName name, Double damage, ElementType elementType, String[] specialties, String ownerUsername) {
        this.Id = Id;
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.specialties = specialties;
        this.ownerUsername = ownerUsername;
    }

    public abstract void displayCardInfo();

    public void upgradeCard(Double upgradeAmount) {
        // increase the damage of the card when upgraded
        Double upgradedDamage = getDamage() + upgradeAmount;
        setDamage(upgradedDamage);

        System.out.println(getName() + " upgraded! New damage: " + upgradedDamage);
    }

    public abstract Double calculateEffectiveDamage(Card opponentCard);

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
