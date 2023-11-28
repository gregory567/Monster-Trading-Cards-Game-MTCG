package org.example;

public abstract class Card {
    protected String name;
    protected static Integer damage;
    protected String elementType;

    public abstract void getAttributes();

    public abstract void displayCardInfo();

    public abstract void upgradeCard();
}
