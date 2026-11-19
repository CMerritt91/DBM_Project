package dbmproject.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import dbmproject.models.Pet;

@Controller
public class petController {

    private final DataSource dataSource;

    public petController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/pet/{id}")
    public String petInfo(@PathVariable int id, Model model) throws SQLException {
        String sql = "SELECT p.PetID, p.Name, p.DOB, p.Description, p.IntakeDate, p.PhotoURL, " +
                     "p.AdoptionStatus, p.Sex, b.BreedName, s.SpeciesName, k.KennelNumber, k.Location " +
                     "FROM Pet p " +
                     "JOIN Breed b ON b.BreedID = p.pBreedID " +
                     "JOIN Species s ON s.SpeciesID = b.bSpeciesID " +
                     "LEFT JOIN Kennel k ON k.KennelID = p.pKennelID " +
                     "WHERE p.PetID = ?";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "redirect:/gallery";
                    }
                    Pet p = new Pet();
                    p.setPetId(rs.getInt("PetID"));
                    p.setName(rs.getString("Name"));
                    p.setDob(rs.getObject("DOB", LocalDate.class));
                    p.setDescription(rs.getString("Description"));
                    p.setIntakeDate(rs.getObject("IntakeDate", LocalDate.class));
                    p.setPhotoUrl(rs.getString("PhotoURL"));
                    p.setAdoptionStatus(rs.getString("AdoptionStatus"));
                    p.setSex(rs.getString("Sex"));
                    model.addAttribute("pet", p);
                    model.addAttribute("breedName", rs.getString("BreedName"));
                    model.addAttribute("speciesName", rs.getString("SpeciesName"));
                    model.addAttribute("kennelNumber", rs.getObject("KennelNumber"));
                    model.addAttribute("kennelLocation", rs.getString("Location"));
                }
            }
            model.addAttribute("history", loadHistory(conn, id));
        }
        return "pet";
    }

    /** One row per visit (and per vaccination recorded at that visit). */
    private List<Map<String, Object>> loadHistory(Connection conn, int petId) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT m.VisitDate, m.VisitType, v.VaccinationType, v.SpayedNeutered " +
                     "FROM MedicalHistory m " +
                     "LEFT JOIN Vaccinations v ON v.vMedicalRecordID = m.MedicalRecordID " +
                     "WHERE m.mPetID = ? ORDER BY m.VisitDate DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("visitDate", rs.getObject("VisitDate", LocalDate.class));
                    row.put("visitType", rs.getString("VisitType"));
                    row.put("vaccinationType", rs.getString("VaccinationType"));
                    row.put("spayedNeutered", rs.getString("SpayedNeutered"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}