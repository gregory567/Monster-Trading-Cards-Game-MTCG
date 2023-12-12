package org.example;

import lombok.Setter;
import lombok.Getter;

@Setter
@Getter
public class Profile {
    private String name;
    private String bio;
    private String image;

    public Profile(String name, String bio, String image) {
        this.name = name;
        this.bio = bio;
        this.image = image;
    }

    public void displayProfile() {
        System.out.println("Name: " + name);
        System.out.println("Bio: " + bio);
        System.out.println("Image: " + image);
    }
}
