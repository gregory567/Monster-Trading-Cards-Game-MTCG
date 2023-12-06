package org.example;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import org.example.Round;

@Getter
@Setter
public class BattleLog {
    private List<Round> rounds;
    private String outcome;

    public BattleLog() {
        this.rounds = new ArrayList<>();
        this.outcome = null;
    }

    public void logRound(Round round) {
        rounds.add(round);
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}

