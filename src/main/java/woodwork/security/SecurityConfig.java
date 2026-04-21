package woodwork.security;

import static org.springframework.http.HttpMethod.*;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // password cryptography engine
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // disable csrf => we are using JWTs, not browser cookies
            .csrf(csrf -> csrf.disable())
            
            // set session management to stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // access rules
            .authorizeHttpRequests(auth -> auth
                // anyone can access the register and login endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // uploaded images
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/webhook/stripe/**").permitAll()
                .requestMatchers(GET, "/api/products/**").permitAll()
                // other requests require authentification
                .anyRequest().authenticated()
                
            );
        
            http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // allow vite development server
        configuration.setAllowedOrigins(List.of("http://localhost:5174")); 
        
        // allow standard http methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // allow the headers the react app will send
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // apply rules to all endpoints
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
