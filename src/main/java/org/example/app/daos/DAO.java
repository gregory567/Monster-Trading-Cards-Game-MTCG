package org.example.app.daos;

import java.util.ArrayList;

public interface DAO<T> {
    Integer create(String username, String password);
    ArrayList<T> readAll();
    T read(String username);
    Integer updateUser(String username, String token, String name, String bio, String image);
    Integer loginUser(String username, String password);
    void delete(String username);
}
