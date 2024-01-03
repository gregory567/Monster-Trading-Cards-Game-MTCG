package org.example;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class MonsterCard extends Card {

    public MonsterCard(UUID Id, CardName name, Double damage, ElementType elementType, String[] specialties, String ownerUsername) {
        super(Id, name, damage, elementType, specialties, ownerUsername);
        this.cardType = CardType.MONSTER;
    }

    @Override
    public void displayCardInfo() {
        System.out.println("MonsterCard Information:");
        System.out.println("Id: " + getId());
        System.out.println("Name: " + getName());
        System.out.println("Damage: " + getDamage());
        System.out.println("ElementType: " + getElementType());
        System.out.println("Specialties: " + specialtiesToString());
    }

    @Override
    public Double calculateEffectiveDamage(Card opponentCard) {
        ElementType opponentElementType = opponentCard.getElementType();
        CardType opponentCardType = opponentCard.getCardType();
        Double baseDamage = getDamage();

        // Check if it's a pure monster fight (no effect based on element type)
        if (opponentCardType == CardType.MONSTER) {
            System.out.println("Pure monster fight! Effective Damage against a monster card: " + baseDamage);
            return baseDamage;
        }

        // Element-based spell attack
        switch (getElementType()) {
            case WATER:
                switch (opponentElementType) {
                    case FIRE:
                        System.out.println("Effective Damage against FIRE: " + (baseDamage * 2));
                        return baseDamage * 2;
                    case NORMAL:
                        System.out.println("Effective Damage against NORMAL: " + (baseDamage / 2));
                        return baseDamage / 2;
                    default:
                        System.out.println("No Effect against WATER: " + baseDamage);
                        return baseDamage;
                }
            case FIRE:
                switch (opponentElementType) {
                    case NORMAL:
                        System.out.println("Effective Damage against NORMAL: " + (baseDamage * 2));
                        return baseDamage * 2;
                    case WATER:
                        System.out.println("Effective Damage against WATER: " + (baseDamage / 2));
                        return baseDamage / 2;
                    default:
                        System.out.println("No Effect against FIRE: " + baseDamage);
                        return baseDamage;
                }
            case NORMAL:
                switch (opponentElementType) {
                    case WATER:
                        System.out.println("Effective Damage against WATER: " + (baseDamage * 2));
                        return baseDamage * 2;
                    case FIRE:
                        System.out.println("Effective Damage against FIRE: " + (baseDamage / 2));
                        return baseDamage / 2;
                    default:
                        System.out.println("No Effect against NORMAL: " + baseDamage);
                        return baseDamage;
                }
            default:
                System.out.println("Invalid Element Type: " + getElementType());
                return baseDamage;
        }
    }

}
