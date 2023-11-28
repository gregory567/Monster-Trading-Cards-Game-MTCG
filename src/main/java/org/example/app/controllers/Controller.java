package org.example.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Controller {
    @Setter
    private ObjectMapper objectMapper;

    public Controller() {
        setObjectMapper(new ObjectMapper());
    }
}
