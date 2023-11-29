package org.example;

import lombok.Setter;

@Setter
public class Profile {
    private String name;
    private String email;
    private String otherDetails;

    public void displayProfile() {
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Other Details: " + otherDetails);
    }
}
