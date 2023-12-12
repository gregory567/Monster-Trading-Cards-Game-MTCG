package org.example.app.daos;

import java.util.ArrayList;

public interface DAO<T> {
    void create(String username, String password);
    ArrayList<T> readAll();
    T read(String username);
    void updateUser(String username, String name, String bio, String image);
    void delete(String username);
}
