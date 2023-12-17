package org.example.app.models;

import lombok.Getter;
import lombok.Setter;
import org.example.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Userdata {
    private String username;
    private String name;
    private String bio;
    private String image;

    // Constructors, getters, and setters can be added based on your requirements
    public Userdata(String username, String name, String bio, String image) {
        this.username = username;
        this.name = name;
        this.bio = bio;
        this.image = image;
    }
}
