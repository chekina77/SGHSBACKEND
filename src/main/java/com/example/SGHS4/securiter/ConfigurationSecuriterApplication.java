package com.example.SGHS4.securiter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.GET;


@Configuration
@EnableWebSecurity
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
                        .csrf(csrf -> csrf.disable())
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        .authorizeHttpRequests(
                                authorize ->
                                        authorize

                                                .requestMatchers(
                                                        "/v3/api-docs/**",
                                                        "/swagger-ui/**",
                                                        "/swagger-ui.html"
                                                ).permitAll()
                                                .requestMatchers(POST, "/inscription").permitAll()
                                                .requestMatchers(POST, "/activation").permitAll()
                                                .requestMatchers(POST, "/connexion").permitAll()
                                                .requestMatchers(POST, "/refresh-token").permitAll()
                                                .requestMatchers(POST, "/admin/complete-registration").permitAll()
                                                .requestMatchers(GET, "/admin/pending-personnel/**").permitAll()
                                                .requestMatchers(POST, "/admin/connexion").permitAll()
                                                .requestMatchers("/enregistrements/enregistrer").permitAll()
                                                .requestMatchers("/enregistrements/allenregistrer").permitAll() // Permettre l'accès à l'URL sans authentification
// Permettre l'accès à l'URL sans authentification





                                                .requestMatchers(POST, "/enregistrements").permitAll()
                                                .requestMatchers(POST, "/admin/connexion").permitAll()//
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/patient/**").hasRole("INFIRMIER")






                                                .anyRequest().authenticated()
                        )
                        .sessionManagement(httpSecuritySessionManagementConfigurer ->
                                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                        )
                        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
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

        // Autoriser l'origine du front-end (sans slash à la fin)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        // Autoriser toutes les méthodes HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Autoriser tous les en-têtes (y compris Content-Type)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Exposer certains en-têtes (si besoin, ex: Authorization)
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        // Pas de cookies ou tokens à envoyer dans cette appli pour l’instant
        configuration.setAllowCredentials(false);

        // Appliquer cette configuration à toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // << ICI la vraie correction

        return source;
    }
}
