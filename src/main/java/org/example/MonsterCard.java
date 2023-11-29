package org.example;

import org.example.Specialty;
import org.example.ElementType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonsterCard extends Card {

    public MonsterCard(String name, Integer damage, String elementType, Specialty[] specialties) {
        super(name, damage, elementType, specialties);
    }

    public void evolve() {
        // Implement the logic for evolving a monster card
        System.out.println("MonsterCard evolved!");
    }

    @Override
    public void getAttributes() {
        // Implement the logic to get attributes of a monster card
        System.out.println("Attributes of MonsterCard - Name: " + getName() + ", Damage: " + getDamage() +
                ", ElementType: " + getElementType());
    }

    @Override
    public void displayCardInfo() {
        // Implement the logic to display information about a monster card
        System.out.println("MonsterCard Information - Name: " + getName() + ", Damage: " + getDamage() +
                ", ElementType: " + getElementType());
    }

    @Override
    public void upgradeCard() {
        // Implement the logic to upgrade a monster card
        System.out.println("MonsterCard upgraded!");
    }

    @Override
    public void applySpecialty(Specialty specialty) {
        // Implement the logic to apply a specialty to a monster card
        // (You can use the existing logic from the Card class or customize it for monsters)
        if (getSpecialties() != null) {
            for (Specialty cardSpecialty : getSpecialties()) {
                if (cardSpecialty.getName().equals(specialty.getName())) {
                    cardSpecialty.applySpecialtyEffect(this);
                    // You might want to break here if each card can have only one instance of a specialty
                }
            }
        }
    }

    public void calculateEffectiveDamage(ElementType opponentElementType) {
        // Implement the logic to calculate effective damage based on opponent's ElementType
        int baseDamage = getDamage();

        // Example: If opponentElementType is WATER, double the damage
        if (opponentElementType == ElementType.WATER) {
            int effectiveDamage = baseDamage * 2;
            System.out.println("Effective Damage against WATER: " + effectiveDamage);
        } else {
            System.out.println("Effective Damage against " + opponentElementType + ": " + baseDamage);
        }
    }
}
