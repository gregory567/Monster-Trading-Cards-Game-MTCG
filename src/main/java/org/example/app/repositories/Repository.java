package org.example.app.repositories;

import java.util.List;

public interface Repository<T> {
    List<T> getAll();
    T get(String username);
    Integer add(String username, String password);
    Integer updateUser(String username, String token, String name, String bio, String image);
    void remove(String username);
}
