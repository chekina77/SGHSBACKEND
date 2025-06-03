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

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ConfigurationSecuriterApplication {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    public ConfigurationSecuriterApplication(
            BCryptPasswordEncoder bCryptPasswordEncoder,
            JwtFilter jwtFilter,
            UserDetailsService userDetailsService
    ) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        // Documentation publique
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Auth publique
                        .requestMatchers(
                                POST, "/inscription", "/activation", "/connexion","/deconnexion", "/refresh-token",
                                "/complete-registration", "/admin/complete-registration", "/admin/connexion",
                                "/api/appointments", "/api/appointments/empreinte","/envoyer-nouveau-code"
                        ).permitAll()
                        .requestMatchers(GET, "/api/appointments/empreinte").permitAll()

                        .requestMatchers("/envoyer-nouveau-code", "/modifier-mot-de-passe").permitAll()
                        .requestMatchers("/ws-labresult/**").permitAll() // ou antMatchers selon version

                        .requestMatchers(GET, "/utilisateur/info-connecte").permitAll()
                        .requestMatchers("/medecins/noms").permitAll()
                        .requestMatchers(GET, "/api/patient/decrypted-names").permitAll()

                        .requestMatchers("/enregistrements/enregistrer", "/enregistrements/allenregistrer").permitAll()

                        // Endpoints ngrok
                        .requestMatchers(POST, "https://972e-143-105-152-40.ngrok-free.app/api/biometric**").permitAll()

                        // Admin uniquement
                        .requestMatchers("/admin/**").hasRole("ADMINISTRATEUR")

                        // Medecins & Admin
                        .requestMatchers("/api/patient/**").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET, "/admin/pending-personnel/**").permitAll()
                        .requestMatchers(GET, "/api/appointments/enregistrements/rendezvous").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(POST, "/api/consultations", "/api/livret/create").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET, "/api/consultations").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"/api/appointments/all").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"/api/appointments/appointment-utilisateur").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"/api/appointments/{id}/decrypted").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"/api/appointments/all-patients").hasAnyRole("MEDECIN", "ADMINISTRATEUR")



                        .requestMatchers(POST,"/api/consultations/decrypt").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                                .requestMatchers(GET,"/api/consultations/patient/{patientId}").hasAnyRole("MEDECIN", "ADMINISTRATEUR")

                                .requestMatchers(POST,"/api/consultations").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(POST,"/api/livret/create").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(POST,"/api/patient/patient/{PatientId}").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"/api/livret/me").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"/api/livret/all").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"/api/livret/patient").hasAnyRole("MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"/api/resultats-tests/envoyer/{id}").hasAnyRole("LABORANTIN","MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(POST,"/api/resultats-tests/enregistrer").hasAnyRole("LABORANTIN","MEDECIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"/api/resultats-tests/envoyes").hasAnyRole("LABORANTIN", "ADMINISTRATEUR")
                        // Résultats de laboratoire : accessibles par ADMIN et LABORANTIN
                        .requestMatchers(POST, "/api/labresults").hasAnyRole("LABORANTIN", "ADMINISTRATEUR")
                        .requestMatchers(GET, "/labresults/patient/**").hasAnyRole("LABORANTIN", "ADMINISTRATEUR")
                        .requestMatchers(GET, "/api/labresults").hasAnyRole("LABORANTIN", "ADMINISTRATEUR")
                        .requestMatchers(GET,"api/labresults/patient/{patientId}").hasAnyRole("LABORANTIN", "ADMINISTRATEUR","MEDECIN")












                        // Medecin uniquement
                        .requestMatchers("/api/medecin/**").hasRole("MEDECIN")

                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://d2be-143-105-152-40.ngrok-free.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setMaxAge(86400L);
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
