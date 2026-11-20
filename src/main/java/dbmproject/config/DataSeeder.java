package dbmproject.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Fills AnimalAdoptionDB with starter data:
 * 1 employee (a registered user with an Employee row), 2 species, 5 breeds,
 * 3 kennels and 10 pets.
 *
 * Runs once at startup when app.seed=true is set in application.properties.
 * It does nothing if the employee account already exists, so it is safe to leave on,
 * but it is meant for empty tables (the IDs below are fixed).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String EMP_EMAIL = "chrismerritt1991@yahoo.com";
    private static final String EMP_PASSWORD = "Lian@2020";

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed:false}")
    private boolean enabled;

    public DataSeeder(DataSource dataSource, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!enabled) {
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (employeeExists(conn)) {
                    System.out.println("[DataSeeder] Seed employee already exists, skipping seed.");
                    return;
                }
                seed(conn);
                conn.commit();
                System.out.println("[DataSeeder] Seed data inserted: 1 employee, 2 species, 5 breeds, 3 kennels, 10 pets.");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private boolean employeeExists(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM RegisteredUser WHERE Email = ?")) {
            ps.setString(1, EMP_EMAIL);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void seed(Connection conn) throws SQLException {

        // ---- Species -------------------------------------------------------
        String speciesSql = "INSERT INTO Species(SpeciesID, SpeciesName) VALUES (?, ?)";
        update(conn, speciesSql, 1, "Dog");
        update(conn, speciesSql, 2, "Cat");

        // ---- Breeds (5 dog breeds) -----------------------------------------
        String breedSql = "INSERT INTO Breed(BreedID, BreedName, bSpeciesID) VALUES (?, ?, ?)";
        update(conn, breedSql, 1, "Labrador Retriever", 1);
        update(conn, breedSql, 2, "German Shepherd", 1);
        update(conn, breedSql, 3, "Rottweiler", 1);
        update(conn, breedSql, 4, "Tabby", 2);
        update(conn, breedSql, 5, "Calico", 2);

        // ---- Kennels -------------------------------------------------------
        String kennelSql = "INSERT INTO Kennel(KennelID, KennelNumber, Capacity, Location) VALUES (?, ?, ?, ?)";
        update(conn, kennelSql, 1, 1, 50, "1495 Veterans Memorial Pkwy, Huntsville, TX 77340");
        update(conn, kennelSql, 2, 2, 30, "32632 Wright Rd, Magnolia, TX 77355");
        update(conn, kennelSql, 3, 3, 35, "728 Elm St, Waller, TX 77484");

        // ---- Employee (a registered user with an Employee row) ---------------
        update(conn,
            "INSERT INTO RegisteredUser(UserID, Email, FirstName, LastName, Password, Address, PhoneNumber) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            1, EMP_EMAIL, "Chris", "Merritt", passwordEncoder.encode(EMP_PASSWORD),
            "100 Shelter Road, Austin, TX 78701", "5125550100");

        update(conn,
        "INSERT INTO Employee(EmployeeID, JobTitle, eUserID, Role) VALUES (?, ?, ?, ?)",
        1, "Shelter Manager", 1, "Manager");

        // ---- 10 dogs -------------------------------------------------------
        String petSql =
            "INSERT INTO Pet(PetID, Name, DOB, Description, IntakeDate, PhotoURL, AdoptionStatus, Sex, " +
            "pKennelID, pBreedID, managedbyEmployeeID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // pet(conn, sql, id, name, dob, description, intake date, status, sex, kennelId, breedId)
        pet(conn, petSql, 1,  "Bark Twain", "2021-03-14", "I like long walks, chewing shoes. Long distance relationship with the mailman.", "2026-09-02", "Available", "Male",   1, 3);
        pet(conn, petSql, 2,  "Brad Pit",   "2022-07-09", "Friendly and photogenic. Loves fetch and belly rubs.",                              "2026-09-10", "Available", "Male",   1, 1);
        pet(conn, petSql, 3,  "Bella",      "2020-05-21", "Gentle and patient. Great with children and other dogs.",                           "2026-08-15", "Available", "Female", 1, 5);
        pet(conn, petSql, 4,  "Max",        "2019-11-30", "Loyal and smart. Needs an active home with a yard.",                                "2026-08-20", "Pending",   "Male",   1, 2);
        pet(conn, petSql, 5,  "Luna",       "2023-02-02", "Curious nose, wagging tail. Follows every scent she finds.",                         "2026-09-18", "Available", "Female", 2, 4);
        pet(conn, petSql, 6,  "Charlie",    "2024-01-17", "Playful young dog who loves swimming and tennis balls.",                            "2026-09-25", "Available", "Male",   2, 1);
        pet(conn, petSql, 7,  "Daisy",      "2022-10-05", "Sweet and calm. Happiest curled up next to her person.",                            "2026-10-01", "Available", "Female", 2, 4);
        pet(conn, petSql, 8,  "Rocky",      "2018-06-12", "Strong, steady and affectionate. Already found his family.",                        "2026-07-30", "Adopted",   "Male",   3, 3);
        pet(conn, petSql, 9,  "Sadie",      "2021-12-25", "Confident and trainable. Learns new tricks quickly.",                               "2026-10-08", "Available", "Female", 3, 2);
        pet(conn, petSql, 10, "Cooper",     "2025-04-03", "Energetic puppy-at-heart who loves everyone he meets.",                             "2026-10-15", "Available", "Male",   3, 5);
    

        // Insert information for medical history
        update(conn, "INSERT INTO MedicalHistory(MedicalRecordID, mPetID, VisitType, VisitDate, SpayedNeutered) " +
             "VALUES (?, ?, ?, ?, ?)", 1, 1, "Checkup", LocalDate.parse("2026-09-05"), "Yes");
        update(conn, "INSERT INTO Vaccinations(vmPetID, VaccinationID, VaccinationType) VALUES (?, ?, ?)", 1, 1, "Rabies");
        update(conn, "INSERT INTO Vaccinations(vmPetID, VaccinationID, VaccinationType) VALUES (?, ?, ?)", 1, 2, "Distemper");
    }

    /** Inserts one pet. All photos use the same placeholder file name (put the image in static/img). */
    private void pet(Connection conn, String sql, int id, String name, String dob, String description,
                     String intake, String status, String sex, int kennelId, int breedId) throws SQLException {
        update(conn, sql, id, name, LocalDate.parse(dob), description, LocalDate.parse(intake),
               "sus_dog.png", status, sex, kennelId, breedId, 1);
    }

    /** Runs one INSERT with the given values, in order. */
    private void update(Connection conn, String sql, Object... values) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]);
            }
            ps.executeUpdate();
        }
    }
}