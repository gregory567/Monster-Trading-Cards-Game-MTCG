package org.example.app.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {
    private String connectionString = "jdbc:postgresql://localhost:5432/mtcgdb?user=postgres&password=postgres";
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private Connection connection;

    public DatabaseService() {
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

            // Establish the database connection
            Connection connection = DriverManager.getConnection(connectionString);
            setConnection(connection);
            System.out.println("Connected to the database.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}

