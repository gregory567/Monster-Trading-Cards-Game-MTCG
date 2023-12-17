package org.example.app.repositories;

import java.util.List;

public interface Repository<T> {
    List<T> getAll();
    T get(String username);
    Integer add(String username, String password);
    Integer updateUser(String username, String name, String bio, String image);
    String loginUser(String username, String password);
    void remove(String username);
}
