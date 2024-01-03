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
public class CardDTO {
    @JsonAlias({"Id"})
    private String id;
    @JsonAlias({"Name"})
    private String name;
    @JsonAlias({"Damage"})
    private Integer damage;

}

