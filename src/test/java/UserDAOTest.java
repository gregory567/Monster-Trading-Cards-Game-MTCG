import org.example.app.daos.UserDAO;
import org.example.app.dtos.UserDataDTO;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {

    private static Connection testConnection; // in-memory database connection

    private UserDAO userDAO;

    private static void printFileContents(String fileName) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            System.out.println("File Contents:\n" + content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeAll
    static void beforeAll() {
        // Setup H2 in-memory database connection
        testConnection = createH2Connection();
        // Print the contents of the Schema.sql file for debugging
        printFileContents("Schema.sql");
        // Ensure to execute any database schema initialization scripts here
        executeScript("Schema.sql", testConnection);
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
        executeScript("Reset.sql", testConnection);
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

    private static void executeScript(String scriptPath, Connection connection) {
        try (InputStream inputStream = UserDAOTest.class.getResourceAsStream(scriptPath)) {
            if (inputStream != null) {
                // Print the absolute path for debugging
                System.out.println("Loading script from: " + Paths.get(scriptPath).toAbsolutePath());

                String scriptContent = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().collect(Collectors.joining("\n"));

                try (Statement statement = connection.createStatement()) {
                    statement.execute(scriptContent);
                }
            } else {
                throw new RuntimeException("Script not found: " + scriptPath);
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Error executing script: " + scriptPath, e);
        }
    }

    @Test
    void createUser_Success() {
        int statusCode = userDAO.createUser("testuser", "password");
        assertEquals(201, statusCode);
    }

    @Test
    void createUser_Conflict() {
        // Create a user first
        userDAO.createUser("existinguser", "password");

        // Try to create the same user again
        int statusCode = userDAO.createUser("existinguser", "newpassword");
        assertEquals(409, statusCode);
    }

    @Test
    void getUser_Success() {
        // Create a user
        userDAO.createUser("testuser", "password");

        // Retrieve user data
        UserDataDTO userData = userDAO.getUser("testuser");
        assertNotNull(userData);
    }

    @Test
    void updateUser_Success() {
        // Create a user
        userDAO.createUser("testuser", "password");

        // Update user information
        int statusCode = userDAO.updateUser("testuser", "New Name", "New Bio", "newimage.jpg");
        assertEquals(200, statusCode);

        // Retrieve updated user data
        UserDataDTO updatedUserData = userDAO.getUser("testuser");
        assertNotNull(updatedUserData);
        assertEquals("New Name", updatedUserData.getName());
        assertEquals("New Bio", updatedUserData.getBio());
        assertEquals("newimage.jpg", updatedUserData.getImage());
    }

    @Test
    void loginUser_Success() {
        // Create a user
        userDAO.createUser("testuser", "password");

        // Login with correct credentials
        String token = userDAO.loginUser("testuser", "password");
        assertNotNull(token);
        assertNotEquals("401", token);
    }

    @Test
    void loginUser_Failure_UserNotFound() {
        // Try to login with non-existent user
        String token = userDAO.loginUser("nonexistentuser", "password");
        assertEquals("404", token);
    }

    @Test
    void loginUser_Failure_AuthenticationFailed() {
        // Create a user
        userDAO.createUser("testuser", "password");

        // Try to login with incorrect password
        String token = userDAO.loginUser("testuser", "wrongpassword");
        assertEquals("401", token);
    }
}
