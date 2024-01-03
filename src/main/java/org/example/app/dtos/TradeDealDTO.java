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
public class TradeDealDTO {
    @JsonAlias({"Id"})
    private String id;
    @JsonAlias({"CardToTrade"})
    private String cardToTrade;
    @JsonAlias({"Type"})
    private String cardType;
    @JsonAlias({"MinimumDamage"})
    private Double minimumDamage;

}

