package dbmproject.controllers;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import dbmproject.models.Breed;
import dbmproject.models.Kennel;
import dbmproject.models.Pet;

@Controller
public class registerPetController {

    private final DataSource dataSource;

    public registerPetController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/petRegistration")
    public String showForm(Model model) throws SQLException {
        model.addAttribute("pet", new Pet());
        addDropdowns(model);
        return "petRegistration";
    }

    @PostMapping("/petRegistration")
    public String submit(@ModelAttribute Pet pet, Principal principal, Model model) throws SQLException {
        String sql = "INSERT INTO Pet(Name, DOB, Description, IntakeDate, PhotoURL, AdoptionStatus, Sex, " +
                     "pKennelID, pBreedID, managedbyEmployeeID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {

            Integer employeeId = findEmployeeId(conn, principal.getName());
            if (employeeId == null) {
                model.addAttribute("message", "Error: only employees can register pets.");
                model.addAttribute("pet", pet);
                addDropdowns(model);
                return "petRegistration";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pet.getName());
                ps.setObject(2, pet.getDob());
                ps.setString(3, pet.getDescription());
                ps.setObject(4, pet.getIntakeDate() != null ? pet.getIntakeDate() : LocalDate.now());
                ps.setString(5, pet.getPhotoUrl());
                ps.setString(6, pet.getAdoptionStatus() != null ? pet.getAdoptionStatus() : "Available");
                ps.setString(7, pet.getSex());
                ps.setInt(8, pet.getKennelId());
                ps.setInt(9, pet.getBreedId());
                ps.setInt(10, employeeId);
                ps.executeUpdate();
            }

            model.addAttribute("message", "Pet registered");
            model.addAttribute("pet", new Pet());
        } catch (SQLException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            model.addAttribute("pet", pet);
        }

        addDropdowns(model);      // always re-add, or the selects come back empty
        return "petRegistration";
    }

    // ---------- helpers ----------

    /** principal.getName() is the login email; find that user's Employee row. */
    private Integer findEmployeeId(Connection conn, String email) throws SQLException {
        String sql = "SELECT e.EmployeeID FROM Employee e " +
                     "JOIN RegisteredUser u ON u.UserID = e.eUserID WHERE u.Email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("EmployeeID") : null;
            }
        }
    }

    private void addDropdowns(Model model) throws SQLException {
        model.addAttribute("kennels", loadKennels());
        model.addAttribute("breeds", loadBreeds());
    }

    private List<Kennel> loadKennels() throws SQLException {
        List<Kennel> kennels = new ArrayList<>();
        String sql = "SELECT KennelID, KennelNumber, Capacity, Location FROM Kennel ORDER BY KennelNumber";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Kennel k = new Kennel();
                k.setKennelId(rs.getInt("KennelID"));
                k.setKennelNumber(rs.getInt("KennelNumber"));
                k.setCapacity(rs.getInt("Capacity"));
                k.setLocation(rs.getString("Location"));
                kennels.add(k);
            }
        }
        return kennels;
    }

    private List<Breed> loadBreeds() throws SQLException {
        List<Breed> breeds = new ArrayList<>();
        String sql = "SELECT BreedID, BreedName FROM Breed ORDER BY BreedName";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Breed b = new Breed();
                b.setBreedId(rs.getInt("BreedID"));
                b.setBreedName(rs.getString("BreedName"));
                breeds.add(b);
            }
        }
        return breeds;
    }
}
