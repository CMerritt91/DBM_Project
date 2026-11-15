package dbmproject.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

     @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // public: static files and pages anyone can see
                .requestMatchers("/css/**","/js/**", "/img/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/gallery", "/login", "/registration", "/adoption", "/pet", "/error", "/api/users").permitAll()
                // everything else needs a login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/", true)     // where to go after a good login
                
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());

        return http.build();
    }
}
