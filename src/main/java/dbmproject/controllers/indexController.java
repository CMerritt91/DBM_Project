package dbmproject.controllers;

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

import dbmproject.models.Kennel;
import dbmproject.models.Pet;

@Controller
public class indexController {

    private final DataSource dataSource;

    public indexController(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
     @GetMapping("/")
    public String home(Model model) throws SQLException {
        model.addAttribute("pets", loadFeaturedPets());
        model.addAttribute("kennels", loadKennels());
        return "index";
    }

    private List<Kennel> loadKennels() throws SQLException {
        List<Kennel> kennels = new ArrayList<>();
        // occupied = pets in the kennel that have not been adopted yet
        String sql =
            "SELECT k.KennelID, k.KennelNumber, k.Capacity, k.Location " +
            "FROM Kennel k " +
            "LEFT JOIN Pet p ON p.pKennelID = k.KennelID AND p.AdoptionStatus <> 'Adopted' " +
            "GROUP BY k.KennelID, k.KennelNumber, k.Capacity, k.Location " +
            "ORDER BY k.KennelNumber";

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

    private List<Pet> loadFeaturedPets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT PetID, Name, DOB, Description, PhotoURL FROM Pet " +
                 "WHERE AdoptionStatus = 'Available' ORDER BY PetID LIMIT 3";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pet p = new Pet();
                p.setPetId(rs.getInt("PetID"));
                p.setName(rs.getString("Name"));
                p.setDob(rs.getObject("DOB", LocalDate.class));
                p.setDescription(rs.getString("Description"));
                p.setPhotoUrl(rs.getString("PhotoURL"));
                pets.add(p);
            }
        }
        return pets;
    }

}
