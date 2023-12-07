package org.example;

import org.example.TradeDeal;
import java.util.ArrayList;
import java.util.List;

/*
The decision to make the list of trade deals static is based on the idea that there should be a single list of trade deals shared across all instances of the Store class.
Making the list static means that it belongs to the class itself rather than a particular instance of the class.

Reasons for this design choice:
Shared State:
If each instance of the Store class had its own list of trade deals, it could lead to confusion and potential issues.
For example, adding a trade deal to one instance of Store would not make it visible to other instances.
Consistent Data:
By making the list static, it ensures that all instances of the Store class operate on the same data.
This can be beneficial in scenarios where we want consistency in the available trades across different parts of the application.
 */
public class Store {
    private static List<TradeDeal> tradeDeals = new ArrayList<>();

    public static void addTradeDeal(TradeDeal tradeDeal) {
        tradeDeals.add(tradeDeal);
    }

    public static void removeTradeDeal(TradeDeal tradeDeal) {
        tradeDeals.remove(tradeDeal);
    }

    public static void displayAvailableTrades() {
        System.out.println("Available Trades:");
        for (TradeDeal deal : tradeDeals) {
            System.out.println("Offering User: " + deal.getOfferingUser().getUsername());
            System.out.println("Offered Card: " + deal.getOfferedCard().getName());
            System.out.println("Requirement: " + deal.getRequirement());
            System.out.println("--------------");
        }
    }
}

