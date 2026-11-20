package dbmproject.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Decides where to send a user right after a good login. */
    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler(DataSource dataSource) {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

        return (request, response, authentication) -> {

            // if they were sent to the login page from a protected page (like Adopt), go back there
            SavedRequest saved = requestCache.getRequest(request, response);
            if (saved != null) {
                requestCache.removeRequest(request, response);
                response.sendRedirect(saved.getRedirectUrl());
                return;
            }

            boolean employee = false;
            String sql = "SELECT 1 FROM Employee e " +
                         "JOIN RegisteredUser u ON u.UserID = e.eUserID WHERE u.Email = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, authentication.getName());      // the login email
                try (ResultSet rs = ps.executeQuery()) {
                    employee = rs.next();
                }
            } catch (SQLException e) {
                employee = false;                               // on a DB problem, treat as a normal user
            }

            response.sendRedirect(request.getContextPath() + (employee ? "/employee" : "/user"));
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationSuccessHandler loginSuccessHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // public: static files and pages anyone can see
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/gallery", "/login", "/registration", "/pet/**", "/error", "/api/users").permitAll()
                // everything else needs a login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(loginSuccessHandler)     // replaces defaultSuccessUrl
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());

        return http.build();
    }
}