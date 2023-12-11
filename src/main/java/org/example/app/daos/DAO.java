package org.example.app.daos;

import java.util.ArrayList;

public interface DAO<T> {
    void create(T type);
    ArrayList<T> readAll();
    T read(int id);
    void update(T type);
    void delete(int id);
}
