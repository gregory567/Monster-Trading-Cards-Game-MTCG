package org.example;

import org.example.Specialty;
import org.example.ElementType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpellCard extends Card {

    public SpellCard(String name, Integer damage, String elementType, Specialty[] specialties) {
        super(name, damage, elementType, specialties);
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
    public void applySpecialty(Specialty specialty) {
        if (getSpecialties() != null) {
            for (Specialty cardSpecialty : getSpecialties()) {
                if (cardSpecialty.getName().equals(specialty.getName())) {
                    cardSpecialty.applySpecialtyEffect(this);
                    // you might want to break here if each card can have only one instance of a specialty
                }
            }
        }
    }

    public void castSpell() {

        System.out.println("SpellCard is casting a spell!");
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
