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

        System.out.println("MonsterCard evolved!");
    }

    @Override
    public void getAttributes() {
        System.out.println("MonsterCard Attributes:");
        System.out.println("Name: " + getName());
        System.out.println("Damage: " + getDamage());
        System.out.println("ElementType: " + getElementType());
    }

    @Override
    public void displayCardInfo() {
        // Implement the logic to display information about a monster card
        System.out.println("MonsterCard Information:");
        System.out.println("Name: " + getName());
        System.out.println("Damage: " + getDamage());
        System.out.println("ElementType: " + getElementType());
        System.out.println("Specialties: " + specialtiesToString());
    }

    @Override
    public void upgradeCard() {

        // increase the damage of the monster when upgraded
        int upgradedDamage = getDamage() + 10;
        setDamage(upgradedDamage);

        System.out.println("MonsterCard upgraded! New damage: " + upgradedDamage);
    }

    @Override
    public void applySpecialty(Specialty specialty) {
        // Implement the logic to apply a specialty to a monster card
        if (getSpecialties() != null) {
            for (Specialty cardSpecialty : getSpecialties()) {
                if (cardSpecialty.getName().equals(specialty.getName())) {
                    cardSpecialty.applySpecialtyEffect(this);
                    // You might want to break here if each card can have only one instance of a specialty
                }
            }
        }
    }

    @Override
    public void calculateEffectiveDamage(ElementType opponentElementType) {
        // calculate effective damage based on opponent's ElementType
        int baseDamage = getDamage();

        // Example: Customized elemental effectiveness logic
        int effectiveDamage;
        switch (opponentElementType) {
            case WATER:
                effectiveDamage = baseDamage / 2;  // half damage against water
                break;
            case FIRE:
                effectiveDamage = baseDamage * 2;  // double damage against fire
                break;
            case NORMAL:
            default:
                effectiveDamage = baseDamage;  // no effect against normal
                break;
        }
        System.out.println("Effective Damage against " + opponentElementType + ": " + effectiveDamage);
    }

    private String specialtiesToString() {
        if (getSpecialties() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Specialty specialty : getSpecialties()) {
                stringBuilder.append(specialty.getName()).append(", ");
            }
            // remove the trailing comma and space
            return stringBuilder.substring(0, stringBuilder.length() - 2);
        }
        return "None";
    }
}
