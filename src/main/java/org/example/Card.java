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

    public static final String GOBLIN_SPECIALTY = "Goblin";
    public static final String WIZZARD_SPECIALTY = "Wizzard";
    public static final String KNIGHT_SPECIALTY = "Knight";
    public static final String KRAKEN_SPECIALTY = "Kraken";
    public static final String FIREELF_SPECIALTY = "FireElf";
    public static final String ORK_SPECIALTY = "Ork";
    public static final String DRAGON_SPECIALTY = "Dragon";

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

    public void applySpecialty(String specialty, Card opponentCard) {
        if (getSpecialties() != null) {
            for (String cardSpecialty : getSpecialties()) {
                if (cardSpecialty.equals(specialty)) {
                    // logic to apply the specialty effect to the card
                    // This method modifies the card based on the specialty

                    // Check the specialty type and apply the corresponding effect
                    switch (specialty) {
                        case GOBLIN_SPECIALTY:
                            // Goblins are too afraid of Dragons to attack
                            if (opponentCard.getSpecialties() != null &&
                                    containsSpecialty(opponentCard.getSpecialties(), DRAGON_SPECIALTY)) {
                                // set damage to 0
                                this.setDamage(0);
                            }
                            break;

                        case WIZZARD_SPECIALTY:
                            // Wizzard can control Orks so they are not able to damage them
                            if (opponentCard.getSpecialties() != null &&
                                    containsSpecialty(opponentCard.getSpecialties(), ORK_SPECIALTY)) {
                                // set damage to 0
                                opponentCard.setDamage(0);
                            }
                            break;

                        case KNIGHT_SPECIALTY:
                            // The armor of Knights is so heavy that WaterSpells make them drown instantly
                            if (opponentCard.getElementType().equals(ElementType.WATER)) {
                                // set damage to 0
                                opponentCard.setDamage(100);
                            }
                            break;

                        case KRAKEN_SPECIALTY:
                            // The Kraken is immune against spells
                            if (opponentCard instanceof SpellCard) {
                                // set damage to 0
                                opponentCard.setDamage(0);
                            }
                            break;

                        case FIREELF_SPECIALTY:
                            // The FireElves know Dragons since they were little and can evade their attacks
                            if (opponentCard.getSpecialties() != null &&
                                    containsSpecialty(opponentCard.getSpecialties(), DRAGON_SPECIALTY)) {
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
