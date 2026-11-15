package dbmproject.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final DataSource dataSource;

    public AppUserDetailsService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String sql = "SELECT Email, Password FROM RegisteredUser WHERE Email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // fully qualified: Spring's User, not your models.User
                    return org.springframework.security.core.userdetails.User
                            .withUsername(rs.getString("Email"))
                            .password(rs.getString("Password"))
                            .roles("USER")
                            .build();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up user", e);
        }

        throw new UsernameNotFoundException("No account with that email");
    }
}