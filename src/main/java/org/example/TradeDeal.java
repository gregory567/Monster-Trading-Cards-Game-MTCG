package org.example;

import org.example.app.models.User;
import lombok.Setter;
import lombok.Getter;
import java.util.UUID;

@Setter
@Getter
public class TradeDeal {
    private UUID id;
    private User offeringUser;
    private Card offeredCard;
    private Requirement requirement;
    private String status;

    public TradeDeal(UUID id, User offeringUser, Card offeredCard, Requirement requirement, String status) {
        this.id = id;
        this.offeringUser = offeringUser;
        this.offeredCard = offeredCard;
        this.requirement = requirement;
        this.status = status;
    }
}
