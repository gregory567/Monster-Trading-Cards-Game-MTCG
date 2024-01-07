import org.example.app.daos.UserDAO;
import org.example.app.dtos.UserDataDTO;
import org.example.app.dtos.UserStatDTO;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {

    private static Connection testConnection; // in-memory database connection

    private UserDAO userDAO;

    // Strings to store SQL script content
    private static String schemaSql;
    private static String resetSql;

    @BeforeAll
    static void beforeAll() {
        // Setup H2 in-memory database connection
        testConnection = createH2Connection();

        // Load the content of Schema.sql and Reset.sql into strings
        schemaSql = loadScriptAsString("src/test/java/Schema.sql");
        resetSql = loadScriptAsString("src/test/java/Reset.sql");

        // Print the contents of the Schema.sql file for debugging
        System.out.println("Schema SQL Contents:\n" + schemaSql);

        // Print the contents of the Reset.sql file for debugging
        System.out.println("Reset SQL Contents:\n" + resetSql);

        // Ensure to execute any database schema initialization scripts here
        executeScript(schemaSql, testConnection);
    }

    @BeforeEach
    void beforeEach() {
        // Create a clean instance of UserDAO for each test
        userDAO = new UserDAO(testConnection);
        // Ensure the database is in a clean state for each test
        resetDatabase();
    }

    @AfterAll
    static void afterAll() {
        // Close the H2 in-memory database connection or clean up resources
        try {
            testConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetDatabase() {
        // Execute SQL scripts or commands to reset the database to a clean state
        // This might include deleting all data, resetting sequences, etc.
        executeScript(resetSql, testConnection);
    }

    private static Connection createH2Connection() {
        try {
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"); // Creating an in-memory database

            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating H2 connection for testing", e);
        }
    }

    private static void executeScript(String scriptContent, Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(scriptContent);
        } catch (SQLException e) {
            throw new RuntimeException("Error executing script", e);
        }
    }

    private static String loadScriptAsString(String absolutePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(absolutePath))) {
            // Print the absolute path for debugging
            System.out.println("Loading script from: " + Paths.get(absolutePath).toAbsolutePath());

            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Error loading script file: " + absolutePath, e);
        }
    }

    @Test
    void createUser_Success() {
        // A - arrange, given
        // A - act, when
        int statusCode = userDAO.createUser("testuser", "password");
        // A - assert, then
        assertEquals(201, statusCode);
    }

    @Test
    void createUser_Conflict() {
        // A - arrange, given
        // Create a user first
        userDAO.createUser("existinguser", "password");

        // A - act, when
        // Try to create the same user again
        int statusCode = userDAO.createUser("existinguser", "newpassword");
        // A - assert, then
        assertEquals(409, statusCode);
    }

    @Test
    void getUser_Success() {
        // A - arrange, given
        // Create a user
        userDAO.createUser("testuser", "password");

        // A - act, when
        // Retrieve user data
        UserDataDTO userData = userDAO.getUser("testuser");
        // A - assert, then
        assertNotNull(userData);
    }

    @Test
    void updateUser_Success() {
        // A - arrange, given
        // Create a user
        userDAO.createUser("testuser", "password");

        // A - act, when
        // Update user information
        int statusCode = userDAO.updateUser("testuser", "New Name", "New Bio", "newimage.jpg");
        // A - assert, then
        assertEquals(200, statusCode);

        // A - act, when
        // Retrieve updated user data
        UserDataDTO updatedUserData = userDAO.getUser("testuser");
        // A - assert, then
        assertNotNull(updatedUserData);
        assertEquals("New Name", updatedUserData.getName());
        assertEquals("New Bio", updatedUserData.getBio());
        assertEquals("newimage.jpg", updatedUserData.getImage());
    }

    @Test
    void loginUser_Success() {
        // A - arrange, given
        // Create a user
        userDAO.createUser("testuser", "password");

        // A - act, when
        // Login with correct credentials
        String token = userDAO.loginUser("testuser", "password");
        // A - assert, then
        assertNotNull(token);
        assertNotEquals("401", token);
    }

    @Test
    void loginUser_Failure_UserNotFound() {
        // A - arrange, given
        // A - act, when
        // Try to login with non-existent user
        String token = userDAO.loginUser("nonexistentuser", "password");
        // A - assert, then
        assertEquals("404", token);
    }

    @Test
    void loginUser_Failure_AuthenticationFailed() {
        // A - arrange, given
        // Create a user
        userDAO.createUser("testuser", "password");

        // A - act, when
        // Try to login with incorrect password
        String token = userDAO.loginUser("testuser", "wrongpassword");
        // A - assert, then
        assertEquals("401", token);
    }

    @Test
    void deleteUser_Success() {
        // A - arrange, given
        // Create a user
        userDAO.createUser("userToDelete", "password");

        // A - act, when
        // Delete the user
        userDAO.deleteUser("userToDelete");

        // Verify that the user is deleted by trying to retrieve their data
        UserDataDTO deletedUserData = userDAO.getUser("userToDelete");
        // A - assert, then
        assertNull(deletedUserData);
    }

    @Test
    void getStats_Success() {
        // A - arrange, given
        // Create a user
        userDAO.createUser("userWithStats", "password");

        // A - act, when
        // Retrieve stats for the user
        UserStatDTO userStats = userDAO.getStats("userWithStats");

        // A - assert, then
        // Verify that the user stats are not null
        assertNotNull(userStats);
        // Add more assertions based on the expected data in the user stats
    }

    @Test
    void getStats_NonExistentUser() {
        // A - arrange, given
        // Attempt to retrieve stats for a non-existent user
        UserStatDTO userStats = userDAO.getStats("nonExistentUser");

        // A - act, when
        // A - assert, then
        // Verify that the user stats are null
        assertNull(userStats);
    }

    @Test
    void getScoreboard_Success() {
        // A - arrange, given
        // Create multiple users with different ELO scores
        userDAO.createUser("user1", "password");
        userDAO.createUser("user2", "password");
        userDAO.createUser("user3", "password");

        // A - act, when
        // Retrieve the scoreboard
        List<UserStatDTO> scoreboard = userDAO.getScoreBoard();

        // A - assert, then
        // Verify that the scoreboard is not null and has the expected number of entries
        assertNotNull(scoreboard);
        assertEquals(3, scoreboard.size());

        // Add more assertions based on the expected order and data in the scoreboard
    }

    @Test
    void getScoreboard_EmptyScoreboard() {
        // A - arrange, given
        // Retrieve the scoreboard when no users are created
        List<UserStatDTO> scoreboard = userDAO.getScoreBoard();

        // A - act, when
        // A - assert, then
        // Verify that the scoreboard is not null and is empty
        assertNotNull(scoreboard);
        assertTrue(scoreboard.isEmpty());
    }
}
