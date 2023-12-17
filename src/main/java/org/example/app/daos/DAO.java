package org.example.app.daos;

import java.util.ArrayList;

public interface DAO<T> {
    Integer create(String username, String password);
    ArrayList<T> readAll();
    T read(String username);
    Integer updateUser(String username, String name, String bio, String image);
    String loginUser(String username, String password);
    void delete(String username);
}
