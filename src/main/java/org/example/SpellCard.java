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

    public SpellCard(CardName name, Integer damage, ElementType elementType, String[] specialties, String owner) {
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
    public Integer calculateEffectiveDamage(Card opponentCard) {
        ElementType opponentElementType = opponentCard.getElementType();
        CardType opponentCardType = opponentCard.getCardType();
        int baseDamage = getDamage();

        // we don't need to check if it's a monster or a spell we are fighting against

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
