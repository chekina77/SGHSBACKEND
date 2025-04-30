package com.example.SGHS4.securiter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.DELETE;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ConfigurationSecuriterApplication {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    public ConfigurationSecuriterApplication(BCryptPasswordEncoder bCryptPasswordEncoder, JwtFilter jwtFilter, UserDetailsService userDetailsService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return
                httpSecurity
                        .csrf(AbstractHttpConfigurer::disable) // Pour les API REST, le CSRF n'est généralement pas nécessaire
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        .authorizeHttpRequests(
                                authorize ->
                                        authorize
                                                // Documentation API
                                                .requestMatchers(
                                                        "/v3/api-docs/**",
                                                        "/swagger-ui/**",
                                                        "/swagger-ui.html"
                                                ).permitAll()

                                                // Endpoints d'authentification publics
                                                .requestMatchers(POST, "/inscription").permitAll()
                                                .requestMatchers(POST, "/activation").permitAll()
                                                .requestMatchers(POST, "/connexion").permitAll()
                                                .requestMatchers(POST, "/refresh-token").permitAll()

                                                // Admin registration endpoints
                                                .requestMatchers(POST, "/admin/complete-registration").permitAll()
                                                .requestMatchers(GET, "/admin/pending-personnel/**").permitAll()
                                                .requestMatchers(POST, "/admin/connexion").permitAll()

                                                // Points d'accès pour l'enregistrement
                                                .requestMatchers("/enregistrements/enregistrer").permitAll()
                                                .requestMatchers("/enregistrements/allenregistrer").permitAll()

                                                // Protections par rôle
                                                .requestMatchers("/admin/**").hasRole("ADMINISTRATEUR") // Rôle ADMIN pour les routes /admin
                                                .requestMatchers("/api/patient/**").hasAnyRole("INFIRMIER", "MEDECIN", "ADMINISTRATEUR") // Routes patients accessibles par INFIRMIER et MEDECIN
                                                .requestMatchers("/api/medecin/**").hasRole("MEDECIN") // Routes réservées aux médecins

                                                // Par défaut, toutes les autres requêtes nécessitent une authentification
                                                .anyRequest().authenticated()
                        )
                        .sessionManagement(session ->
                                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Pas de session côté serveur
                        )
                        .authenticationProvider(authenticationProvider())
                        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // Filtre JWT avant l'authentification par défaut
                        .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Liste des origines autorisées (à ajuster selon vos besoins)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://votre-domaine-production.com"));

        // Autoriser toutes les méthodes HTTP nécessaires
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Autoriser tous les en-têtes standards
        configuration.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With"
        ));

        // En-têtes exposés au client
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));

        // Durée de préflight en secondes (86400 = 24h)
        configuration.setMaxAge(86400L);

        // Les cookies ne sont pas envoyés dans cette application
        configuration.setAllowCredentials(false);

        // Appliquer cette configuration à toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}