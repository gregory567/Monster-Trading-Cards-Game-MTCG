package org.example;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Round {
    private UUID roundId;
    private Battle battle;
    private Integer roundNumber;
    private String winner;
    private String loser;
    private boolean draw;
    private RoundDetail roundDetail;
}
