package org.example.app.repositories;

import java.util.List;

public interface Repository<T> {
    List<T> getAll();
    T get(String username);
    void add(String username, String password);
    void updateUser(String username, String name, String bio, String image);
    void remove(String username);
}
