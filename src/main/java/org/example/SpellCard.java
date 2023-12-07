package org.example;

import lombok.Getter;
import lombok.Setter;
import org.example.app.models.User;

import static org.example.ElementType.FIRE;
import static org.example.ElementType.WATER;
import static org.example.ElementType.NORMAL;

@Getter
@Setter
public class SpellCard extends Card {

    public SpellCard(String name, Integer damage, String elementType, String[] specialties, User owner) {
        super(name, damage, elementType, specialties, owner);
        this.cardType = CardType.SPELL;
    }

    @Override
    public void getAttributes() {
        System.out.println("SpellCard Attributes:");
        System.out.println("Name: " + getName());
        System.out.println("Damage: " + getDamage());
        System.out.println("ElementType: " + getElementType());
    }

    @Override
    public void displayCardInfo() {
        System.out.println("SpellCard Information:");
        System.out.println("Name: " + getName());
        System.out.println("Damage: " + getDamage());
        System.out.println("ElementType: " + getElementType());
        System.out.println("Specialties: " + specialtiesToString());
    }

    @Override
    public void upgradeCard() {
        // increase the damage of the spell when upgraded
        int upgradedDamage = getDamage() + 10;
        setDamage(upgradedDamage);

        System.out.println("SpellCard upgraded! New damage: " + upgradedDamage);
    }

    @Override
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
                            if (opponentCard.getElementType() == ElementType.WATER) {
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

    public void castSpell() {
        System.out.println("SpellCard is casting a spell!");
    }

    @Override
    public void calculateEffectiveDamage(ElementType opponentElementType, CardType opponentCardType) {
        int baseDamage = getDamage();

        // we don't need to check if it's a monster or a spell we are fighting against

        // Element-based spell attack
        switch (getElementType()) {
            case WATER:
                switch (opponentElementType) {
                    case FIRE:
                        System.out.println("Effective Damage against FIRE: " + (baseDamage * 2));
                        break;
                    case NORMAL:
                        System.out.println("Effective Damage against NORMAL: " + (baseDamage / 2));
                        break;
                    default:
                        System.out.println("No Effect against WATER: " + baseDamage);
                        break;
                }
                break;
            case FIRE:
                switch (opponentElementType) {
                    case NORMAL:
                        System.out.println("Effective Damage against NORMAL: " + (baseDamage * 2));
                        break;
                    case WATER:
                        System.out.println("Effective Damage against WATER: " + (baseDamage / 2));
                        break;
                    default:
                        System.out.println("No Effect against FIRE: " + baseDamage);
                        break;
                }
                break;
            case NORMAL:
                switch (opponentElementType) {
                    case WATER:
                        System.out.println("Effective Damage against WATER: " + (baseDamage * 2));
                        break;
                    case FIRE:
                        System.out.println("Effective Damage against FIRE: " + (baseDamage / 2));
                        break;
                    default:
                        System.out.println("No Effect against NORMAL: " + baseDamage);
                        break;
                }
                break;
            default:
                System.out.println("Invalid Element Type: " + getElementType());
                break;
        }
    }

}
