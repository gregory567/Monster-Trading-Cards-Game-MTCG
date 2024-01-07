import org.example.app.daos.UserDAO;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.example.app.daos.CardDAO;
import org.example.app.dtos.CardDTO;

import java.util.Arrays;
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
public class CardDAOTest {

    private static Connection testConnection; // in-memory database connection
    private CardDAO cardDAO;
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
        cardDAO = new CardDAO(testConnection);

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
    public void testCreatePackage() {

        // Create a new package and assert the expected result
        List<CardDTO> cards = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0),
                new CardDTO("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", "Ork", 45.0),
                new CardDTO("dfdd758f-649c-40f9-ba3a-8657f4b3439f", "FireSpell", 25.0)
        );
        Integer result = cardDAO.createPackage(cards);
        assertEquals(201, result);
        // Add more assertions based on your expected behavior
    }

    @Test
    public void testCreatePackageWithLessThan5Cards() {

        // Create a package with only 3 cards
        List<CardDTO> cards = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0)
        );

        int statusCode = cardDAO.createPackage(cards);

        assertEquals(400, statusCode, "Should return 400 for less than 5 cards");
    }

    @Test
    public void testCreatePackageWithDuplicateCardIds() {

        // Create a package with 5 cards, but some cards have the same id
        List<CardDTO> cards = Arrays.asList(
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "WaterGoblin", 9.0),
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "Dragon", 55.0),
                new CardDTO("91a6471b-1426-43f6-ad65-6fc473e16f9f", "WaterSpell", 21.0),
                new CardDTO("4ec8b269-0dfa-4f97-809a-2c63fe2a0025", "Ork", 55.0),
                new CardDTO("f8043c23-1534-4487-b66b-238e0c3c39b5", "WaterSpell", 23.0)
        );

        int statusCode = cardDAO.createPackage(cards);

        assertEquals(400, statusCode, "Should return 400 for duplicate card ids");
    }

    @Test
    public void testCreatePackageWithCardsAlreadyInOtherPackages() {

        // Create a package with 5 cards, where some cards are already in other packages
        List<CardDTO> cards = Arrays.asList(
                new CardDTO("d7d0cb94-2cbf-4f97-8ccf-9933dc5354b8", "WaterGoblin", 9.0),
                new CardDTO("44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e", "Dragon", 55.0),
                new CardDTO("2c98cd06-518b-464c-b911-8d787216cddd", "WaterSpell", 21.0),
                new CardDTO("951e886a-0fbf-425d-8df5-af2ee4830d85", "Ork", 55.0),
                new CardDTO("dcd93250-25a7-4dca-85da-cad2789f7198", "FireSpell", 23.0)
        );

        // Add the cards to the first package
        cardDAO.createPackage(cards);

        // Attempt to create another package with the same cards
        int statusCode = cardDAO.createPackage(cards);

        // Assert that the result code is 409 (Conflict)
        assertEquals(409, statusCode, "Should return 409 for cards already in other packages");
    }

}
