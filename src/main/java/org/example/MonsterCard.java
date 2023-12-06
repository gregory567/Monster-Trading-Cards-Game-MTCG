package org.example;

import org.example.Specialty;
import org.example.ElementType;
import org.example.CardType;
import lombok.Getter;
import lombok.Setter;
import static org.example.ElementType.FIRE;
import static org.example.ElementType.WATER;
import static org.example.ElementType.NORMAL;

@Getter
@Setter
public class MonsterCard extends Card {

    public MonsterCard(String name, Integer damage, String elementType, Specialty[] specialties) {
        super(name, damage, elementType, specialties);
        this.cardType = CardType.MONSTER;
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
    public void calculateEffectiveDamage(ElementType opponentElementType, CardType opponentCardType) {
        int baseDamage = getDamage();

        // Check if it's a pure monster fight (no effect based on element type)
        if (opponentCardType == CardType.MONSTER) {
            System.out.println("Pure monster fight! Effective Damage against a monster card: " + baseDamage);
            return;
        }

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
