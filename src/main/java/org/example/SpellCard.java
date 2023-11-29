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
        // implement upgradeCard for SpellCard

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

    public void calculateEffectiveDamage(ElementType opponentElementType) {
        // calculate effective damage based on opponent's ElementType
        int baseDamage = getDamage();

        // example: if opponentElementType is FIRE, double the damage
        if (opponentElementType == ElementType.FIRE) {
            int effectiveDamage = baseDamage * 2;
            System.out.println("Effective Damage against FIRE: " + effectiveDamage);
        } else {
            System.out.println("Effective Damage against " + opponentElementType + ": " + baseDamage);
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
