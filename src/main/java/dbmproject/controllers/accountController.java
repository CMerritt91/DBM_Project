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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dbmproject.models.Pet;
import dbmproject.models.User;

@Controller
public class accountController {

    private final DataSource dataSource;

    public accountController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/user")
    public String account(Principal principal, Model model) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            User user = null;
            int userId = 0;

            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT UserID, Email, FirstName, LastName, Address, PhoneNumber " +
                "FROM RegisteredUser WHERE Email = ?")) {
                ps.setString(1, principal.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("UserID");
                        user = new User();
                        user.setEmail(rs.getString("Email"));
                        user.setFirstName(rs.getString("FirstName"));
                        user.setLastName(rs.getString("LastName"));
                        user.setAddress(rs.getString("Address"));
                        user.setPhoneNumber(rs.getString("PhoneNumber"));
                    }
                }
            }

            if (user == null) {
                return "redirect:/login";
            }

            model.addAttribute("user", user);                      // fills the modal form
            model.addAttribute("firstName", user.getFirstName());
            model.addAttribute("adoptedPets", loadAdoptedPets(conn, userId));
            model.addAttribute("isVolunteer", isVolunteer(conn, userId));
        }
        return "user";
    }

    @PostMapping("/user/update")
    public String update(@ModelAttribute User user, Principal principal,
                     RedirectAttributes redirect) throws SQLException {
        String sql = "UPDATE RegisteredUser SET FirstName = ?, LastName = ?, Address = ?, PhoneNumber = ? " +
                 "WHERE Email = ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getAddress());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, principal.getName());      // always the logged-in user, never the form's email
            ps.executeUpdate();
            redirect.addFlashAttribute("message", "Your information was updated.");
        } catch (SQLException e) {
            redirect.addFlashAttribute("error", "Could not update: " + e.getMessage());
        }
        return "redirect:/user";
    }

    private List<Pet> loadAdoptedPets(Connection conn, int userId) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        // assumed columns: adoptionApplication(aUserID, aPetID, ApplicationStatus)
        String sql = "SELECT p.PetID, p.Name, p.DOB, p.PhotoURL, p.Sex " +
             "FROM adoptionApplication a " +
             "JOIN Pet p ON p.PetID = a.aaPetID " +
             "WHERE a.adopterUserID = ? AND a.ApplicationStatus = 'Approved' " +
             "ORDER BY p.Name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pet p = new Pet();
                    p.setPetId(rs.getInt("PetID"));
                    p.setName(rs.getString("Name"));
                    p.setDob(rs.getObject("DOB", LocalDate.class));
                    p.setPhotoUrl(rs.getString("PhotoURL"));
                    p.setSex(rs.getString("Sex"));
                    pets.add(p);
                }
            }
        }
        return pets;
    }

    private boolean isVolunteer(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "SELECT 1 FROM Volunteer WHERE vUserID = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @PostMapping("/user/volunteer")
    public String becomeVolunteer(@RequestParam String availability, Principal principal,
                              RedirectAttributes redirect) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            Integer userId = null;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT UserID FROM RegisteredUser WHERE Email = ?")) {
                ps.setString(1, principal.getName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("UserID");
                    }
                }
            }

            if (userId == null) {
            return "redirect:/login";
            }

            if (isVolunteer(conn, userId)) {
                redirect.addFlashAttribute("message", "You are already a volunteer.");
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Volunteer(Availability, vUserID) VALUES (?, ?)")) {
                    ps.setString(1, availability.trim());
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                redirect.addFlashAttribute("message", "Thank you for volunteering!");
            }
        } catch (SQLException e) {
            redirect.addFlashAttribute("error", "Could not sign you up: " + e.getMessage());
        }
        return "redirect:/user";
    }
}