package dbmproject.controllers;

import java.security.Principal;
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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class employeeController {

    private static final List<String> REPORT_TYPES = List.of(
        "Kennel Maintenance Issues", "Monthly Adoption Summary", "Volunteer Issues", "Appointment Notes");

    private final DataSource dataSource;

    public employeeController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ---------------------------------------------------------------- page

    @GetMapping("/employee")
    public String page(Principal principal, Model model) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            
            Integer empId = findEmployeeId(conn, principal.getName());
            if (empId == null) {
                return "redirect:/";  // not an employee
            }

            model.addAttribute("employeeId", empId);
            model.addAttribute("today", LocalDate.now());

            // pets per kennel (adopted pets are no longer in a kennel)
            List<String> labels = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            String kennelSql =
                "SELECT k.KennelNumber, k.Location, COUNT(p.PetID) AS PetCount " +
                "FROM Kennel k " +
                "LEFT JOIN Pet p ON p.pKennelID = k.KennelID AND p.AdoptionStatus <> 'Adopted' " +
                "GROUP BY k.KennelID, k.KennelNumber, k.Location ORDER BY k.KennelNumber";
            try (PreparedStatement ps = conn.prepareStatement(kennelSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    labels.add("Kennel " + rs.getInt("KennelNumber") + " - " + rs.getString("Location"));
                    counts.add(rs.getInt("PetCount"));
                }
            }
            model.addAttribute("kennelLabels", labels);
            model.addAttribute("kennelCounts", counts);

            // pending adoption applications
            List<Map<String, Object>> pending = new ArrayList<>();
            String pendingSql =
                "SELECT a.ApplicationID, a.DateSubmitted, u.FirstName, u.LastName, u.Email, p.PetID, p.Name " +
                "FROM adoptionApplication a " +
                "JOIN RegisteredUser u ON u.UserID = a.adopterUserID " +
                "JOIN Pet p ON p.PetID = a.aaPetID " +
                "WHERE a.ApplicationStatus = 'Pending' ORDER BY a.DateSubmitted, a.ApplicationID";
            try (PreparedStatement ps = conn.prepareStatement(pendingSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("applicationId", rs.getInt("ApplicationID"));
                    row.put("dateSubmitted", rs.getObject("DateSubmitted", LocalDate.class));
                    row.put("applicant", rs.getString("FirstName") + " " + rs.getString("LastName"));
                    row.put("email", rs.getString("Email"));
                    row.put("petId", rs.getInt("PetID"));
                    row.put("petName", rs.getString("Name"));
                    pending.add(row);
                }
            }
            model.addAttribute("pending", pending);

            // recent reports
            List<Map<String, Object>> reports = new ArrayList<>();
            String reportSql =
                "SELECT r.ReportID, r.ReportType, r.DateGenerated, r.url, u.FirstName, u.LastName " +
                "FROM Reporting r " +
                "JOIN Employee e ON e.EmployeeID = r.GeneratedbyEmployeeID " +
                "JOIN RegisteredUser u ON u.UserID = e.eUserID " +
                "ORDER BY r.DateGenerated DESC, r.ReportID DESC LIMIT 25";
            try (PreparedStatement ps = conn.prepareStatement(reportSql);
                ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("reportId", rs.getInt("ReportID"));
                    row.put("reportType", rs.getString("ReportType"));
                    row.put("dateGenerated", rs.getObject("DateGenerated", LocalDate.class));
                    row.put("generatedBy", rs.getString("FirstName") + " " + rs.getString("LastName"));
                    row.put("url", rs.getString("url"));
                    reports.add(row);
                }
            }
            model.addAttribute("reports", reports);
            model.addAttribute("reportTypes", REPORT_TYPES);
        }
        return "employee";
    }

    // approve / deny

    @PostMapping("/employee/review")
    public String review(@RequestParam int applicationId, @RequestParam String decision,
                         Principal principal, RedirectAttributes redirect) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            Integer empId = findEmployeeId(conn, principal.getName());
            if (empId == null) {
                return "redirect:/";
            }

            if (!"Manager".equals(findEmployeeRole(conn, principal.getName()))) {
                redirect.addFlashAttribute("error", "Only managers can approve or deny applications.");
                return "redirect:/employee";
            }

            conn.setAutoCommit(false);
            try {
                // the application must still be pending
                Integer petId = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT aaPetID FROM adoptionApplication WHERE ApplicationID = ? AND ApplicationStatus = 'Pending'")) {
                    ps.setInt(1, applicationId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) petId = rs.getInt(1);
                    }
                }
                if (petId == null) {
                    conn.rollback();
                    redirect.addFlashAttribute("error", "That application was already reviewed.");
                    return "redirect:/employee";
                }

                if ("approve".equals(decision)) {
                    // approved: record who approved it
                    update(conn, "UPDATE adoptionApplication SET ApplicationStatus = 'Approved', " +
                                 "ReviewedByEmployeeID = ? WHERE ApplicationID = ?", empId, applicationId);
                    // the pet is adopted
                    update(conn, "UPDATE Pet SET AdoptionStatus = 'Adopted' WHERE PetID = ?", petId);
                    // anyone else waiting on the same pet is denied
                    update(conn, "UPDATE adoptionApplication SET ApplicationStatus = 'Denied', " +
                                 "ReviewedByEmployeeID = ? WHERE aaPetID = ? AND ApplicationStatus = 'Pending'",
                           empId, petId);
                    redirect.addFlashAttribute("message", "Application approved. The pet is now marked Adopted.");
                } else {
                    update(conn, "UPDATE adoptionApplication SET ApplicationStatus = 'Denied', " +
                                 "ReviewedByEmployeeID = ? WHERE ApplicationID = ?", empId, applicationId);
                    redirect.addFlashAttribute("message", "Application denied.");
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                redirect.addFlashAttribute("error", "Could not save the decision: " + e.getMessage());
            }
        }
        return "redirect:/employee";
    }

    // ---------------------------------------------------------- report

    @PostMapping("/employee/report")
    public String createReport(@RequestParam String reportType,
                           @RequestParam(required = false) String otherType,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateGenerated,
                           @RequestParam String url,
                           Principal principal, RedirectAttributes redirect) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            Integer empId = findEmployeeId(conn, principal.getName());
            if (empId == null) {
                return "redirect:/";
            }

            String type = reportType;
            if ("Other".equals(reportType)) {
                type = otherType == null ? "" : otherType.trim();
                if (type.isEmpty() || type.length() > 250) {
                    redirect.addFlashAttribute("error", "Enter a report type (up to 250 characters).");
                    return "redirect:/employee";
                }
            } else if (!REPORT_TYPES.contains(reportType)) {
                redirect.addFlashAttribute("error", "Choose a report type from the list.");
                return "redirect:/employee";
            }

            if (dateGenerated.isAfter(LocalDate.now())) {
                redirect.addFlashAttribute("error", "The report date can't be in the future.");
                return "redirect:/employee";
            }

            String reportUrl = url.trim();
            if (reportUrl.isEmpty() || reportUrl.length() > 250) {
                redirect.addFlashAttribute("error", "Message must be less than 250 characters");
                return "redirect:/employee";
            }

            update(conn, "INSERT INTO Reporting(ReportType, DateGenerated, GeneratedbyEmployeeID, url) VALUES (?, ?, ?, ?)",
               type, dateGenerated, empId, reportUrl);
            redirect.addFlashAttribute("message", "Report created: " + type);
        } catch (SQLException e) {
            redirect.addFlashAttribute("error", "Could not create the report: " + e.getMessage());
        }
        return "redirect:/employee";
    }

    // helpers

    /** principal.getName() is the login email; returns that user's EmployeeID or null. */
    private Integer findEmployeeId(Connection conn, String email) throws SQLException {
        String sql = "SELECT e.EmployeeID FROM Employee e " +
                     "JOIN RegisteredUser u ON u.UserID = e.eUserID WHERE u.Email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private void update(Connection conn, String sql, Object... values) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]);
            }
            ps.executeUpdate();
        }
    }

    private String findEmployeeRole(Connection conn, String email) throws SQLException {
        String sql = "SELECT e.Role FROM Employee e " +
                 "JOIN RegisteredUser u ON u.UserID = e.eUserID WHERE u.Email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }
}