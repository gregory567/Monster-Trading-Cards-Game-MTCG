package org.example;
import org.example.Specialty;

public abstract class Card {
    protected String name;
    protected Integer damage;
    protected String elementType;
    protected Specialty[] specialties;

    public abstract void getAttributes();

    public abstract void displayCardInfo();

    public abstract void upgradeCard();

    public void applySpecialty(Specialty specialty) {
        if(specialties != null) {
            for (Specialty cardSpecialty : specialties) {
                if (cardSpecialty.getName().equals(specialty.getName())) {
                    cardSpecialty.applySpecialtyEffect(this);
                    // You might want to break here if each card can have only one instance of a specialty
                }
            }
        }
    }
}
