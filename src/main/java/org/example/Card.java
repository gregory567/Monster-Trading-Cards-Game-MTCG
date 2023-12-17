package org.example;

import org.example.app.models.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Card {

    protected Package.CardName name;
    protected Integer damage;
    protected ElementType elementType;
    protected String[] specialties;
    protected CardType cardType;
    protected User owner;

    public Card(Package.CardName name, Integer damage, ElementType elementType, String[] specialties, User owner) {
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.specialties = specialties;
        this.owner = owner;
    }

    public abstract void getAttributes();

    public abstract void displayCardInfo();

    public abstract void upgradeCard();

    public abstract void calculateEffectiveDamage(ElementType opponentElementType, CardType opponentCardType);

    public void applySpecialty(String specialty, Card opponentCard) {
        if (getSpecialties() != null) {
            for (String cardSpecialty : getSpecialties()) {
                if (cardSpecialty.equals(specialty)) {
                    // Implement the logic to apply the specialty effect to the card
                    // This method should modify the card based on the specialty

                    // Check the specialty type and apply the corresponding effect
                    switch (specialty) {
                        case "Goblin":
                            // Goblins are too afraid of Dragons to attack
                            if (opponentCard.getSpecialties() != null &&
                                    containsSpecialty(opponentCard.getSpecialties(), "Dragon")) {
                                // set damage to 0
                                this.setDamage(0);
                            }
                            break;

                        case "Wizzard":
                            // Wizzard can control Orks so they are not able to damage them
                            if (opponentCard.getSpecialties() != null &&
                                    containsSpecialty(opponentCard.getSpecialties(), "Ork")) {
                                // set damage to 0
                                opponentCard.setDamage(0);
                            }
                            break;

                        case "Knight":
                            // The armor of Knights is so heavy that WaterSpells make them drown instantly
                            if (opponentCard.getElementType().equals(ElementType.WATER)) {
                                // set damage to 0
                                opponentCard.setDamage(100);
                            }
                            break;

                        case "Kraken":
                            // The Kraken is immune against spells
                            if (opponentCard instanceof SpellCard) {
                                // set damage to 0
                                opponentCard.setDamage(0);
                            }
                            break;

                        case "FireElves":
                            // The FireElves know Dragons since they were little and can evade their attacks
                            if (opponentCard.getSpecialties() != null &&
                                    containsSpecialty(opponentCard.getSpecialties(), "Dragon")) {
                                // set damage to 0
                                opponentCard.setDamage(0);
                            }
                            break;

                        default:
                            // Handle other specialties or no effect
                            break;
                    }
                }
            }
        }
    }

    // Helper method to check if the card has a specific specialty
    protected boolean containsSpecialty(String[] specialties, String specialtyToFind) {
        for (String specialty : specialties) {
            if (specialty.equals(specialtyToFind)) {
                return true;
            }
        }
        return false;
    }

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
