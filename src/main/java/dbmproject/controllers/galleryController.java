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
import org.springframework.web.bind.annotation.RequestParam;

import dbmproject.models.Pet;

@Controller
public class galleryController {

    private static final int PAGE_SIZE = 15;

    private final DataSource dataSource;

    public galleryController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/gallery")
    public String gallery(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(required = false) Integer species,
                          @RequestParam(required = false) Integer breed,
                          @RequestParam(required = false) String sex,
                          Model model) throws SQLException {

        if (sex != null && sex.isBlank()) sex = null;

        // the same filters are used for the count and for the page of pets
        List<Object> params = new ArrayList<>();
        String from = buildFrom(species, breed, sex, params);

        int total = countPets(from, params);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        model.addAttribute("pets", loadPets(from, params, page));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);

        // dropdown contents
        model.addAttribute("breedNames", loadNames("SELECT BreedID, BreedName FROM Breed ORDER BY BreedName"));
        model.addAttribute("speciesNames", loadNames("SELECT SpeciesID, SpeciesName FROM Species ORDER BY SpeciesName"));

        // the current selections, so the form and page buttons remember them
        model.addAttribute("species", species);
        model.addAttribute("breed", breed);
        model.addAttribute("sex", sex);
        return "gallery";
    }

    /** Builds the FROM/WHERE part and fills params in the same order as the ? marks. */
    private String buildFrom(Integer species, Integer breed, String sex, List<Object> params) {
        StringBuilder sql = new StringBuilder(
            " FROM Pet p JOIN Breed b ON b.BreedID = p.pBreedID " +
            "WHERE p.AdoptionStatus = 'Available'");
        if (species != null) {
            sql.append(" AND b.bSpeciesID = ?");
            params.add(species);
        }
        if (breed != null) {
            sql.append(" AND p.pBreedID = ?");
            params.add(breed);
        }
        if (sex != null) {
            sql.append(" AND p.Sex = ?");
            params.add(sex);
        }
        return sql.toString();
    }

    private int countPets(String from, List<Object> params) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*)" + from)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<Pet> loadPets(String from, List<Object> params, int page) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT p.PetID, p.Name, p.DOB, p.Description, p.PhotoURL, p.pBreedID, p.Sex" +
                     from + " ORDER BY p.PetID LIMIT ? OFFSET ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object value : params) {
                ps.setObject(i++, value);
            }
            ps.setInt(i++, PAGE_SIZE);
            ps.setInt(i, (page - 1) * PAGE_SIZE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pet p = new Pet();
                    p.setPetId(rs.getInt("PetID"));
                    p.setName(rs.getString("Name"));
                    p.setDob(rs.getObject("DOB", LocalDate.class));
                    p.setDescription(rs.getString("Description"));
                    p.setPhotoUrl(rs.getString("PhotoURL"));
                    p.setBreedId(rs.getInt("pBreedID"));
                    p.setSex(rs.getString("Sex"));
                    pets.add(p);
                }
            }
        }
        return pets;
    }

    /** Runs a query that returns (id, name) and gives back a map of id to name. */
    private Map<Integer, String> loadNames(String sql) throws SQLException {
        Map<Integer, String> names = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                names.put(rs.getInt(1), rs.getString(2));
            }
        }
        return names;
    }
}