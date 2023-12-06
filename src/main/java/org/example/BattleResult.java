package org.example;

import org.example.app.models.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BattleResult {
    private User opponent;
    private String outcome;

    public BattleResult(User opponent, String outcome) {
        this.opponent = opponent;
        this.outcome = outcome;
    }
}
