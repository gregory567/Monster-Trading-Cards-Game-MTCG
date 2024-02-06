import org.example.app.daos.UserDAO;
import org.example.app.daos.CardDAO;
import org.example.app.dtos.CardDTO;
import org.example.app.repositories.CardRepository;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

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

    // String to store SQL DB-schema script content
    private static String schemaSql;
    // String to store SQL DB reset-script content
    private static String resetSql;

    @BeforeAll
    static void beforeAll() {
        // Set up an H2 in-memory database connection
        testConnection = createH2Connection();

        // Load the content of Schema.sql and Reset.sql into strings
        schemaSql = loadScriptAsString("src/test/java/Schema.sql");
        resetSql = loadScriptAsString("src/test/java/Reset.sql");

        // Print the contents of the Schema.sql file for debugging
        System.out.println("Schema SQL Contents:\n" + schemaSql);

        // Print the contents of the Reset.sql file for debugging
        System.out.println("Reset SQL Contents:\n" + resetSql);

        // Execute database schema initialization scripts here
        executeScript(schemaSql, testConnection);
    }

    @BeforeEach
    void beforeEach() {
        // Create a clean instance of CardDAO for each test
        cardDAO = new CardDAO(testConnection);
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
        // Execute SQL script to reset the database to a clean state
        // This includes deleting all data from all tables
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
    public void createPackage_Success() {

        // A - arrange, given
        // Create a new package with 5 valid cards
        List<CardDTO> cards = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0),
                new CardDTO("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", "Ork", 45.0),
                new CardDTO("dfdd758f-649c-40f9-ba3a-8657f4b3439f", "FireSpell", 25.0)
        );

        // A - act, when
        Integer statusCode = cardDAO.createPackage(cards);

        // A - assert, then
        assertEquals(201, statusCode);
    }

    @Test
    public void createPackage_Failure_LessThan5Cards() {

        // A - arrange, given
        // Create a package with only 3 valid cards
        List<CardDTO> cards = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0)
        );

        // A - act, when
        Integer statusCode = cardDAO.createPackage(cards);

        // A - assert, then
        assertEquals(400, statusCode, "Should return 400 for less than 5 cards");
    }

    @Test
    public void createPackage_Failure_DuplicateCardIds() {

        // A - arrange, given
        // Create a package with 5 cards, but some cards have the same id
        List<CardDTO> cards = Arrays.asList(
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "WaterGoblin", 9.0),
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "Dragon", 55.0),
                new CardDTO("91a6471b-1426-43f6-ad65-6fc473e16f9f", "WaterSpell", 21.0),
                new CardDTO("4ec8b269-0dfa-4f97-809a-2c63fe2a0025", "Ork", 55.0),
                new CardDTO("f8043c23-1534-4487-b66b-238e0c3c39b5", "WaterSpell", 23.0)
        );

        // A - act, when
        Integer statusCode = cardDAO.createPackage(cards);

        // A - assert, then
        assertEquals(400, statusCode, "Should return 400 for duplicate card ids");
    }

    @Test
    public void createPackage_Failure_CardsAlreadyInOtherPackages() {

        // A - arrange, given
        // Create a package with 5 cards, where some cards are already in other packages
        List<CardDTO> cards = Arrays.asList(
                new CardDTO("d7d0cb94-2cbf-4f97-8ccf-9933dc5354b8", "WaterGoblin", 9.0),
                new CardDTO("44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e", "Dragon", 55.0),
                new CardDTO("2c98cd06-518b-464c-b911-8d787216cddd", "WaterSpell", 21.0),
                new CardDTO("951e886a-0fbf-425d-8df5-af2ee4830d85", "Ork", 55.0),
                new CardDTO("dcd93250-25a7-4dca-85da-cad2789f7198", "FireSpell", 23.0)
        );

        // A - act, when
        // Add the cards to the first package
        cardDAO.createPackage(cards);

        // Attempt to create another package with the same cards
        Integer statusCode = cardDAO.createPackage(cards);

        // A - assert, then
        assertEquals(409, statusCode, "Should return 409 for cards already in other packages");
    }

    @Test
    public void buyPackage_Success() {
        // A - arrange, given
        // Create a user "testuser" with the UserDAO
        userDAO.createUser("testuser", "password");

        // Create a package with 5 valid cards
        List<CardDTO> cards = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0),
                new CardDTO("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", "Ork", 45.0),
                new CardDTO("dfdd758f-649c-40f9-ba3a-8657f4b3439f", "FireSpell", 25.0)
        );
        cardDAO.createPackage(cards);

        // A - act, when
        // Buy a package for a user
        List<CardDTO> purchasedCards = null;
        try {
            purchasedCards = cardDAO.buyPackage("testuser");
        } catch (CardRepository.InsufficientFundsException | CardRepository.CardPackageNotFoundException e) {
            throw new RuntimeException(e);
        }

        // A - assert, then
        assertEquals("845f0dc7-37d0-426e-994e-43fc3ac83c08", purchasedCards.get(0).getId());
        assertEquals("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", purchasedCards.get(1).getId());
        assertEquals("e85e3976-7c86-4d06-9a80-641c2019a79f", purchasedCards.get(2).getId());
        assertEquals("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", purchasedCards.get(3).getId());
        assertEquals("dfdd758f-649c-40f9-ba3a-8657f4b3439f", purchasedCards.get(4).getId());

        assertEquals("WaterGoblin", purchasedCards.get(0).getName());
        assertEquals("Dragon", purchasedCards.get(1).getName());
        assertEquals("WaterSpell", purchasedCards.get(2).getName());
        assertEquals("Ork", purchasedCards.get(3).getName());
        assertEquals("FireSpell", purchasedCards.get(4).getName());

        assertEquals(10.0, purchasedCards.get(0).getDamage());
        assertEquals(50.0, purchasedCards.get(1).getDamage());
        assertEquals(20.0, purchasedCards.get(2).getDamage());
        assertEquals(45.0, purchasedCards.get(3).getDamage());
        assertEquals(25.0, purchasedCards.get(4).getDamage());

        // Ensure that the purchased cards match the expected cards
        assertEquals(cards.size(), purchasedCards.size());
        for (int i = 0; i < cards.size(); i++) {
            assertEquals(cards.get(i).getId(), purchasedCards.get(i).getId());
            assertEquals(cards.get(i).getName(), purchasedCards.get(i).getName());
            assertEquals(cards.get(i).getDamage(), purchasedCards.get(i).getDamage());
        }

        // Ensure that the user now owns the cards from the package
        List<CardDTO> userCards = cardDAO.getUserCards("testuser");
        assertEquals(cards.size(), userCards.size());
    }


    @Test
    public void buyPackage_Failure_NotEnoughCoins() {
        // A - arrange, given
        // Create a user "testuser" with the UserDAO
        userDAO.createUser("testuser", "password");

        // Create 6 different packages
        List<CardDTO> package_1 = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0),
                new CardDTO("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", "Ork", 45.0),
                new CardDTO("dfdd758f-649c-40f9-ba3a-8657f4b3439f", "FireSpell", 25.0)
        );
        cardDAO.createPackage(package_1);

        List<CardDTO> package_2 = Arrays.asList(
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "WaterGoblin", 9.0),
                new CardDTO("4a2757d6-b1c3-47ac-b9a3-91deab093531", "Dragon", 55.0),
                new CardDTO("91a6471b-1426-43f6-ad65-6fc473e16f9f", "WaterSpell", 21.0),
                new CardDTO("4ec8b269-0dfa-4f97-809a-2c63fe2a0025", "Ork", 55.0),
                new CardDTO("f8043c23-1534-4487-b66b-238e0c3c39b5", "WaterSpell", 23.0)
        );
        cardDAO.createPackage(package_2);

        List<CardDTO> package_3 = Arrays.asList(
                new CardDTO("b017ee50-1c14-44e2-bfd6-2c0c5653a37c", "WaterGoblin", 11.0),
                new CardDTO("d04b736a-e874-4137-b191-638e0ff3b4e7", "Dragon", 70.0),
                new CardDTO("88221cfe-1f84-41b9-8152-8e36c6a354de", "WaterSpell", 22.0),
                new CardDTO("1d3f175b-c067-4359-989d-96562bfa382c", "Ork", 40.0),
                new CardDTO("171f6076-4eb5-4a7d-b3f2-2d650cc3d237", "RegularSpell", 28.0)
        );
        cardDAO.createPackage(package_3);

        List<CardDTO> package_4 = Arrays.asList(
                new CardDTO("ed1dc1bc-f0aa-4a0c-8d43-1402189b33c8", "WaterGoblin", 10.0),
                new CardDTO("65ff5f23-1e70-4b79-b3bd-f6eb679dd3b5", "Dragon", 50.0),
                new CardDTO("55ef46c4-016c-4168-bc43-6b9b1e86414f", "WaterSpell", 20.0),
                new CardDTO("f3fad0f2-a1af-45df-b80d-2e48825773d9", "Ork", 45.0),
                new CardDTO("8c20639d-6400-4534-bd0f-ae563f11f57a", "WaterSpell", 25.0)
        );
        cardDAO.createPackage(package_4);

        List<CardDTO> package_5 = Arrays.asList(
                new CardDTO("d7d0cb94-2cbf-4f97-8ccf-9933dc5354b8", "WaterGoblin", 9.0),
                new CardDTO("44c82fbc-ef6d-44ab-8c7a-9fb19a0e7c6e", "Dragon", 55.0),
                new CardDTO("2c98cd06-518b-464c-b911-8d787216cddd", "WaterSpell", 21.0),
                new CardDTO("951e886a-0fbf-425d-8df5-af2ee4830d85", "Ork", 55.0),
                new CardDTO("dcd93250-25a7-4dca-85da-cad2789f7198", "FireSpell", 23.0)
        );
        cardDAO.createPackage(package_5);

        List<CardDTO> package_6 = Arrays.asList(
                new CardDTO("b2237eca-0271-43bd-87f6-b22f70d42ca4", "WaterGoblin", 11.0),
                new CardDTO("9e8238a4-8a7a-487f-9f7d-a8c97899eb48", "Dragon", 70.0),
                new CardDTO("d60e23cf-2238-4d49-844f-c7589ee5342e", "WaterSpell", 22.0),
                new CardDTO("fc305a7a-36f7-4d30-ad27-462ca0445649", "Ork", 40.0),
                new CardDTO("84d276ee-21ec-4171-a509-c1b88162831c", "RegularSpell", 28.0)
        );
        cardDAO.createPackage(package_6);


        // A - act, when
        // Buy the first four packages for the user
        for (int i = 0; i < 4; i++) {
            try {
                cardDAO.buyPackage("testuser");
            } catch (CardRepository.InsufficientFundsException | CardRepository.CardPackageNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        // Attempt to buy the fifth package for the user
        List<CardDTO> purchasedCards = null;
        try {
            purchasedCards = cardDAO.buyPackage("testuser");
        } catch (CardRepository.InsufficientFundsException e) {
            // Expected behavior, do nothing
        } catch (CardRepository.CardPackageNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Ensure that the purchase failed due to insufficient funds
        assertNull(purchasedCards);

        // Ensure that the user only owns cards from the first four packages
        List<CardDTO> userCards = cardDAO.getUserCards("testuser");
        assertEquals(20, userCards.size());
    }

    @Test
    public void buyPackage_Failure_PackageNotFound() {
        // A - arrange, given
        // Create a user "testuser" with the UserDAO
        userDAO.createUser("testuser", "password");

        // Create 2 different packages
        List<CardDTO> package_1 = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0),
                new CardDTO("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", "Ork", 45.0),
                new CardDTO("dfdd758f-649c-40f9-ba3a-8657f4b3439f", "FireSpell", 25.0)
        );
        cardDAO.createPackage(package_1);

        List<CardDTO> package_2 = Arrays.asList(
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "WaterGoblin", 9.0),
                new CardDTO("4a2757d6-b1c3-47ac-b9a3-91deab093531", "Dragon", 55.0),
                new CardDTO("91a6471b-1426-43f6-ad65-6fc473e16f9f", "WaterSpell", 21.0),
                new CardDTO("4ec8b269-0dfa-4f97-809a-2c63fe2a0025", "Ork", 55.0),
                new CardDTO("f8043c23-1534-4487-b66b-238e0c3c39b5", "WaterSpell", 23.0)
        );
        cardDAO.createPackage(package_2);

        // A - act, when
        // Buy the first two packages for the user
        for (int i = 0; i < 2; i++) {
            try {
                cardDAO.buyPackage("testuser");
            } catch (CardRepository.InsufficientFundsException | CardRepository.CardPackageNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        // Attempt to buy the third (non-existent) package for the user
        List<CardDTO> purchasedCards = null;
        try {
            purchasedCards = cardDAO.buyPackage("testuser");
        } catch (CardRepository.InsufficientFundsException e) {
            throw new RuntimeException(e);
        } catch (CardRepository.CardPackageNotFoundException e) {
            // Expected behavior, do nothing
        }

        // A - assert, then
        // Ensure that the purchase failed due to lack of packages
        assertNull(purchasedCards);

        // Ensure that the user only owns cards from the first four packages
        List<CardDTO> userCards = cardDAO.getUserCards("testuser");
        assertEquals(10, userCards.size());
    }

    @Test
    public void updateDeck_Failure_LessThan4UniqueCards() {
        // A - arrange, given
        // Create a user "testuser" with the UserDAO
        userDAO.createUser("testuser", "password");

        // Create 2 different packages
        List<CardDTO> package_1 = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0),
                new CardDTO("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", "Ork", 45.0),
                new CardDTO("dfdd758f-649c-40f9-ba3a-8657f4b3439f", "FireSpell", 25.0)
        );
        cardDAO.createPackage(package_1);

        List<CardDTO> package_2 = Arrays.asList(
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "WaterGoblin", 9.0),
                new CardDTO("4a2757d6-b1c3-47ac-b9a3-91deab093531", "Dragon", 55.0),
                new CardDTO("91a6471b-1426-43f6-ad65-6fc473e16f9f", "WaterSpell", 21.0),
                new CardDTO("4ec8b269-0dfa-4f97-809a-2c63fe2a0025", "Ork", 55.0),
                new CardDTO("f8043c23-1534-4487-b66b-238e0c3c39b5", "WaterSpell", 23.0)
        );
        cardDAO.createPackage(package_2);

        // A - act, when
        // Buy the first two packages for the user
        for (int i = 0; i < 2; i++) {
            try {
                cardDAO.buyPackage("testuser");
            } catch (CardRepository.InsufficientFundsException | CardRepository.CardPackageNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        // Configure the user's deck with 3 cards from the purchased packages
        List<CardDTO> userCards = cardDAO.getUserCards("testuser");
        // taking the first 3 cards in user's stack to form the deck
        List<String> deckCardIds = userCards.subList(0, 3).stream().map(CardDTO::getId).collect(Collectors.toList());

        Integer updateStatus = cardDAO.updateUserDeck("testuser", deckCardIds);
        List<CardDTO> UserDeck = cardDAO.getDeckCards("testuser");

        // A - assert, then
        assertEquals(Integer.valueOf(400), updateStatus);
        // Verify that the deck is not null and is empty
        assertNotNull(UserDeck);
        assertTrue(UserDeck.isEmpty());
    }

    @Test
    public void updateDeck_Failure_CardsNotInUserStack() {
        // A - arrange, given
        // Create a user "testuser" with the UserDAO
        userDAO.createUser("testuser", "password");

        // Create 2 different packages
        List<CardDTO> package_1 = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0),
                new CardDTO("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", "Ork", 45.0),
                new CardDTO("dfdd758f-649c-40f9-ba3a-8657f4b3439f", "FireSpell", 25.0)
        );
        cardDAO.createPackage(package_1);

        List<CardDTO> package_2 = Arrays.asList(
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "WaterGoblin", 9.0),
                new CardDTO("4a2757d6-b1c3-47ac-b9a3-91deab093531", "Dragon", 55.0),
                new CardDTO("91a6471b-1426-43f6-ad65-6fc473e16f9f", "WaterSpell", 21.0),
                new CardDTO("4ec8b269-0dfa-4f97-809a-2c63fe2a0025", "Ork", 55.0),
                new CardDTO("f8043c23-1534-4487-b66b-238e0c3c39b5", "WaterSpell", 23.0)
        );
        cardDAO.createPackage(package_2);

        // A - act, when
        // Buy the first package for the user
        try {
            cardDAO.buyPackage("testuser");
        } catch (CardRepository.InsufficientFundsException | CardRepository.CardPackageNotFoundException e) {
            throw new RuntimeException(e);
        }

        // taking the first 4 cards in the second package (does not belong to the user) to form the deck
        List<String> deckCardIds = package_2.subList(0, 4).stream().map(CardDTO::getId).collect(Collectors.toList());

        Integer updateStatus = cardDAO.updateUserDeck("testuser", deckCardIds);
        List<CardDTO> UserDeck = cardDAO.getDeckCards("testuser");

        // A - assert, then
        assertEquals(Integer.valueOf(403), updateStatus);
        // Verify that the deck is not null and is empty
        assertNotNull(UserDeck);
        assertTrue(UserDeck.isEmpty());
    }

    @Test
    public void getUserDeck_Failure_UnconfiguredDeck() {
        // A - arrange, given
        // Create a user "testuser" with the UserDAO
        userDAO.createUser("testuser", "password");

        // Create 4 different packages
        List<CardDTO> package_1 = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0),
                new CardDTO("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", "Ork", 45.0),
                new CardDTO("dfdd758f-649c-40f9-ba3a-8657f4b3439f", "FireSpell", 25.0)
        );
        cardDAO.createPackage(package_1);

        List<CardDTO> package_2 = Arrays.asList(
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "WaterGoblin", 9.0),
                new CardDTO("4a2757d6-b1c3-47ac-b9a3-91deab093531", "Dragon", 55.0),
                new CardDTO("91a6471b-1426-43f6-ad65-6fc473e16f9f", "WaterSpell", 21.0),
                new CardDTO("4ec8b269-0dfa-4f97-809a-2c63fe2a0025", "Ork", 55.0),
                new CardDTO("f8043c23-1534-4487-b66b-238e0c3c39b5", "WaterSpell", 23.0)
        );
        cardDAO.createPackage(package_2);

        List<CardDTO> package_3 = Arrays.asList(
                new CardDTO("b017ee50-1c14-44e2-bfd6-2c0c5653a37c", "WaterGoblin", 11.0),
                new CardDTO("d04b736a-e874-4137-b191-638e0ff3b4e7", "Dragon", 70.0),
                new CardDTO("88221cfe-1f84-41b9-8152-8e36c6a354de", "WaterSpell", 22.0),
                new CardDTO("1d3f175b-c067-4359-989d-96562bfa382c", "Ork", 40.0),
                new CardDTO("171f6076-4eb5-4a7d-b3f2-2d650cc3d237", "RegularSpell", 28.0)
        );
        cardDAO.createPackage(package_3);

        List<CardDTO> package_4 = Arrays.asList(
                new CardDTO("ed1dc1bc-f0aa-4a0c-8d43-1402189b33c8", "WaterGoblin", 10.0),
                new CardDTO("65ff5f23-1e70-4b79-b3bd-f6eb679dd3b5", "Dragon", 50.0),
                new CardDTO("55ef46c4-016c-4168-bc43-6b9b1e86414f", "WaterSpell", 20.0),
                new CardDTO("f3fad0f2-a1af-45df-b80d-2e48825773d9", "Ork", 45.0),
                new CardDTO("8c20639d-6400-4534-bd0f-ae563f11f57a", "WaterSpell", 25.0)
        );
        cardDAO.createPackage(package_4);

        // A - act, when
        // Buy the first four packages for the user
        for (int i = 0; i < 4; i++) {
            try {
                cardDAO.buyPackage("testuser");
            } catch (CardRepository.InsufficientFundsException | CardRepository.CardPackageNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        List<CardDTO> UserDeck = cardDAO.getDeckCards("testuser");

        // A - assert, then
        // Verify that the deck is not null and is empty
        assertNotNull(UserDeck);
        assertTrue(UserDeck.isEmpty());
    }

    @Test
    public void getUserDeck_Success_ConfiguredDeck() {
        // A - arrange, given
        // Create a user "testuser" with the UserDAO
        userDAO.createUser("testuser", "password");

        // Create 4 different packages
        List<CardDTO> package_1 = Arrays.asList(
                new CardDTO("845f0dc7-37d0-426e-994e-43fc3ac83c08", "WaterGoblin", 10.0),
                new CardDTO("99f8f8dc-e25e-4a95-aa2c-782823f36e2a", "Dragon", 50.0),
                new CardDTO("e85e3976-7c86-4d06-9a80-641c2019a79f", "WaterSpell", 20.0),
                new CardDTO("1cb6ab86-bdb2-47e5-b6e4-68c5ab389334", "Ork", 45.0),
                new CardDTO("dfdd758f-649c-40f9-ba3a-8657f4b3439f", "FireSpell", 25.0)
        );
        cardDAO.createPackage(package_1);

        List<CardDTO> package_2 = Arrays.asList(
                new CardDTO("644808c2-f87a-4600-b313-122b02322fd5", "WaterGoblin", 9.0),
                new CardDTO("4a2757d6-b1c3-47ac-b9a3-91deab093531", "Dragon", 55.0),
                new CardDTO("91a6471b-1426-43f6-ad65-6fc473e16f9f", "WaterSpell", 21.0),
                new CardDTO("4ec8b269-0dfa-4f97-809a-2c63fe2a0025", "Ork", 55.0),
                new CardDTO("f8043c23-1534-4487-b66b-238e0c3c39b5", "WaterSpell", 23.0)
        );
        cardDAO.createPackage(package_2);

        List<CardDTO> package_3 = Arrays.asList(
                new CardDTO("b017ee50-1c14-44e2-bfd6-2c0c5653a37c", "WaterGoblin", 11.0),
                new CardDTO("d04b736a-e874-4137-b191-638e0ff3b4e7", "Dragon", 70.0),
                new CardDTO("88221cfe-1f84-41b9-8152-8e36c6a354de", "WaterSpell", 22.0),
                new CardDTO("1d3f175b-c067-4359-989d-96562bfa382c", "Ork", 40.0),
                new CardDTO("171f6076-4eb5-4a7d-b3f2-2d650cc3d237", "RegularSpell", 28.0)
        );
        cardDAO.createPackage(package_3);

        List<CardDTO> package_4 = Arrays.asList(
                new CardDTO("ed1dc1bc-f0aa-4a0c-8d43-1402189b33c8", "WaterGoblin", 10.0),
                new CardDTO("65ff5f23-1e70-4b79-b3bd-f6eb679dd3b5", "Dragon", 50.0),
                new CardDTO("55ef46c4-016c-4168-bc43-6b9b1e86414f", "WaterSpell", 20.0),
                new CardDTO("f3fad0f2-a1af-45df-b80d-2e48825773d9", "Ork", 45.0),
                new CardDTO("8c20639d-6400-4534-bd0f-ae563f11f57a", "WaterSpell", 25.0)
        );
        cardDAO.createPackage(package_4);

        // A - act, when
        // Buy the first four packages for the user
        for (int i = 0; i < 4; i++) {
            try {
                cardDAO.buyPackage("testuser");
            } catch (CardRepository.InsufficientFundsException | CardRepository.CardPackageNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        // Configure the user's deck with 4 cards from the purchased packages
        List<CardDTO> userCards = cardDAO.getUserCards("testuser");
        // taking the first 4 cards in user's stack to form the deck
        List<CardDTO> deckCards = userCards.subList(0, 4);
        List<String> deckCardIds = userCards.subList(0, 4).stream().map(CardDTO::getId).collect(Collectors.toList());

        Integer updateStatus = cardDAO.updateUserDeck("testuser", deckCardIds);

        // save the user's newly configured deck
        List<CardDTO> userDeck = cardDAO.getDeckCards("testuser");

        // A - assert, then
        assertEquals(Integer.valueOf(200), updateStatus);
        // Ensure that the retrieved user deck matches the configured deck
        assertEquals(deckCards.size(), userDeck.size());
        for (int i = 0; i < deckCards.size(); i++) {
            assertEquals(deckCards.get(i).getId(), userDeck.get(i).getId());
            assertEquals(deckCards.get(i).getName(), userDeck.get(i).getName());
            assertEquals(deckCards.get(i).getDamage(), userDeck.get(i).getDamage());
        }
    }

}
