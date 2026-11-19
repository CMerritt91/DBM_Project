package dbmproject.controllers;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.sql.DataSource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dbmproject.models.Pet;
import dbmproject.models.User;

@Controller
public class adoptionController {

    private final DataSource dataSource;

    public adoptionController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/adoption")
    public String form(@RequestParam int petId, Principal principal,
                       Model model, RedirectAttributes redirect) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {

            // the pet, filled into the form
            String petSql = "SELECT p.PetID, p.Name, p.DOB, p.PhotoURL, p.Sex, p.AdoptionStatus, b.BreedName " +
                            "FROM Pet p JOIN Breed b ON b.BreedID = p.pBreedID WHERE p.PetID = ?";
            try (PreparedStatement ps = conn.prepareStatement(petSql)) {
                ps.setInt(1, petId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "redirect:/gallery";
                    }
                    if (!"Available".equals(rs.getString("AdoptionStatus"))) {
                        redirect.addFlashAttribute("error", "That pet is not available for adoption right now.");
                        return "redirect:/pet/" + petId;
                    }
                    Pet p = new Pet();
                    p.setPetId(rs.getInt("PetID"));
                    p.setName(rs.getString("Name"));
                    p.setDob(rs.getObject("DOB", LocalDate.class));
                    p.setPhotoUrl(rs.getString("PhotoURL"));
                    p.setSex(rs.getString("Sex"));
                    model.addAttribute("pet", p);
                    model.addAttribute("breedName", rs.getString("BreedName"));
                }
            }

            // the logged-in applicant, filled into the form
            String userSql = "SELECT FirstName, LastName, Email, Address, PhoneNumber " +
                             "FROM RegisteredUser WHERE Email = ?";
            try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                ps.setString(1, principal.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "redirect:/login";
                    }
                    User u = new User();
                    u.setFirstName(rs.getString("FirstName"));
                    u.setLastName(rs.getString("LastName"));
                    u.setEmail(rs.getString("Email"));
                    u.setAddress(rs.getString("Address"));
                    u.setPhoneNumber(rs.getString("PhoneNumber"));
                    model.addAttribute("user", u);
                }
            }
        }
        return "adoption";
    }

    @PostMapping("/adoption")
    public String submit(@RequestParam int petId, Principal principal,
                         RedirectAttributes redirect) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {

            Integer userId = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT UserID FROM RegisteredUser WHERE Email = ?")) {
                ps.setString(1, principal.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) userId = rs.getInt("UserID");
                }
            }
            if (userId == null) {
                return "redirect:/login";
            }

            // the pet must still be available
            boolean available = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT AdoptionStatus FROM Pet WHERE PetID = ?")) {
                ps.setInt(1, petId);
                try (ResultSet rs = ps.executeQuery()) {
                    available = rs.next() && "Available".equals(rs.getString(1));
                }
            }
            if (!available) {
                redirect.addFlashAttribute("error", "That pet is not available for adoption right now.");
                return "redirect:/pet/" + petId;
            }

            // no second application for the same pet
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM adoptionApplication WHERE adopterUserID = ? AND aaPetID = ? " +
                    "AND ApplicationStatus IN ('Pending', 'Approved')")) {
                ps.setInt(1, userId);
                ps.setInt(2, petId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        redirect.addFlashAttribute("message", "You already have an application for this pet.");
                        return "redirect:/user";
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO adoptionApplication(DateSubmitted, ApplicationStatus, adopterUserID, aaPetID) " +
                    "VALUES (?, 'Pending', ?, ?)")) {
                ps.setObject(1, LocalDate.now());
                ps.setInt(2, userId);
                ps.setInt(3, petId);
                ps.executeUpdate();
            }
            redirect.addFlashAttribute("message", "Your adoption application was submitted.");
        } catch (SQLException e) {
            redirect.addFlashAttribute("error", "Could not submit the application: " + e.getMessage());
            return "redirect:/pet/" + petId;
        }
        return "redirect:/user";
    }
}