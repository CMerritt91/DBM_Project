package controllers;

import models.User;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import io.github.cdimascio.dotenv.Dotenv;

@Controller
public class registerController {

    Dotenv dotenv = Dotenv.load();
    String url = "jdbc:mysql://localhost:3306/AnimalAdoptionDB";
    String sqlUser = "root";
    String password = dotenv.get("DB_PASSWORD");

    @GetMapping("/registration")
    public String showForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String submit(@ModelAttribute User user, Model model) {
        String sql = "INSERT INTO RegisteredUser(Email, FirstName, LastName, Password, Address, PhoneNumber) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, sqlUser, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getAddress());
            ps.setString(6, user.getPhoneNumber());
            ps.executeUpdate();
            model.addAttribute("user", new User());
            model.addAttribute("message", "Account Created");
        } catch (SQLException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "registration";
    }
}
