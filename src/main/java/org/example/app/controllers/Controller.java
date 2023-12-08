package org.example.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Controller {

    private ObjectMapper objectMapper;

    public Controller() {
        setObjectMapper(new ObjectMapper());
    }
}
