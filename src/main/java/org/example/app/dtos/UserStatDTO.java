package org.example.app.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatDTO {
    @JsonAlias({"Name"})
    private String name;
    @JsonAlias({"Elo"})
    private String elo_score;
    @JsonAlias({"Wins"})
    private String wins;
    @JsonAlias({"Losses"})
    private String losses;
}
