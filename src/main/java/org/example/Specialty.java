package org.example;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Specialty {
    private String name;

    public Specialty(String name) {
        this.name = name;
    }

    public void applySpecialtyEffect(Card card) {
        // Implement the logic to apply the specialty effect to the card
        // This method should modify the card based on the specialty

        // Check the specialty type and apply the corresponding effect
        switch (name) {
            case "Goblin":
                // Goblins are too afraid of Dragons to attack
                if (card.getName().equals("Dragon")) {
                    // set damage to 0
                    card.setDamage(0);
                }
                break;

            case "Wizzard":
                // Wizzard can control Orks so they are not able to damage them
                if (card.getName().equals("Ork")) {
                    // set damage to 0
                    card.setDamage(0);
                }
                break;

            case "Knight":
                // The armor of Knights is so heavy that WaterSpells make them drown instantly
                if (card.getElementType() == ElementType.WATER) {
                    // set damage to 0
                    card.setDamage(0);
                }
                break;

            case "Kraken":
                // The Kraken is immune against spells
                if (card instanceof SpellCard) {
                    // set damage to 0
                    card.setDamage(0);
                }
                break;

            case "FireElves":
                // The FireElves know Dragons since they were little and can evade their attacks
                if (card.getName().equals("Dragon")) {
                    // set damage to 0
                    card.setDamage(0);
                }
                break;

            default:
                // Handle other specialties or no effect
                break;
        }
    }
}

