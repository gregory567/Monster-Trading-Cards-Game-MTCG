package org.example.app.models;

import org.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class City {
    @JsonAlias({"id"})
    int id;
    @JsonAlias({"name"})
    String name;
    @JsonAlias({"population"})
    int population;

    // Jackson needs the default constructor
    public City() {}
}
