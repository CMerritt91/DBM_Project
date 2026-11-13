package dbmproject.config;


// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// public class SecurityConfiguration {

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             .authorizeHttpRequests(auth -> auth
//                 // public: static files and pages anyone can see
//                 .requestMatchers("/css/**", "/img/**").permitAll()
//                 .requestMatchers("/", "/gallery", "/login", "/registration", "/login", "/adoption", "/pet", "/api/users").permitAll()
//                 // everything else needs a login
//                 .anyRequest().authenticated()
//             )
//             .formLogin(form -> form
//                 .loginPage("/login")
//                 .permitAll()
//             )
//             .logout(logout -> logout.permitAll());

//         return http.build();
//     }
// }
