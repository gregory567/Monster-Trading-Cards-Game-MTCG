package org.example.app.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeDealDTO {
    private String id;
    private String cardToTrade;
    private String cardType;
    private Double minimumDamage;

}

