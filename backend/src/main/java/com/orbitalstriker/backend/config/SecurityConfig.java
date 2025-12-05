package com.orbitalstriker.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            
            .csrf(csrf -> csrf.disable())
            
            
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/phpmyadmin/**").permitAll() // Adatbázis konzol
                .requestMatchers("/api/**").permitAll()        // Csapat lekérő API
                .requestMatchers("/ws/**").permitAll()         // WebSocket játékhoz
                .anyRequest().permitAll()                    
            );
            
        return http.build();
    }

    // CORS beállítás, hogy a Frontend (localhost:3000 vagy 5173) elérje a Backendet
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // Itt add meg a frontend címeidet (React alapból 3000, Vite 5173)
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}