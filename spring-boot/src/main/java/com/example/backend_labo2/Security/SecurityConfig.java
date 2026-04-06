package com.example.backend_labo2.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import com.example.backend_labo2.jwt_util_java.*;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final jwt_util_filter jwtFilter; // ton filtre JWT existant
    private final CorsConfigurationSource corsConfigurationSource; // injecte le bean unique

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // CSRF OFF
            .csrf(csrf -> csrf.disable())

            // Désactiver login par défaut
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())

            // Gestion JWT stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Autorisations
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()   // endpoints auth (login/register)
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/api/departements/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/users/etudiants/**").permitAll()
                .requestMatchers("/api/users/reservations/**").permitAll()
                .requestMatchers("/api/reclamations/**").authenticated()
                .requestMatchers("/api/users/equipements/**").permitAll()
             // Dans votre SecurityConfig, ajoutez ces endpoints à la liste des URLs publiques :
                .requestMatchers("/api/auth/**").permitAll()
                // Ou spécifiquement :
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/verify-reset-token").permitAll()
                .anyRequest().authenticated() // toutes les autres requêtes nécessitent JWT valide
            );
        
        // Ajouter le filtre JWT
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
