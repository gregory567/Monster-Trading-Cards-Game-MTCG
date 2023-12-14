package org.example.app.daos;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenValidator {
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    Connection connection;

    public TokenValidator(Connection connection) {
        setConnection(connection);
    }

    public boolean validateToken(String token, String username) {
        // Prepare the SQL query
        String selectStmt = "SELECT COUNT(*) FROM UserCredentials WHERE username = ? AND token = ?";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(selectStmt)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, token);

            // Execute the query and obtain the result set
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If the result set has at least one row, the token is valid
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Assume token validation failure on database error
        return false;
    }
}

