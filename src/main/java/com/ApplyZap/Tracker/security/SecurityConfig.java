package com.ApplyZap.Tracker.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity(debug = false)
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/actuator/**").permitAll()
                                                .requestMatchers("/api/user-sync").permitAll()
                                                .requestMatchers("/h2-console/**").permitAll()
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                                                .requestMatchers("/board/**").hasRole("USER")
                                                .requestMatchers("/api/analytics/**").hasRole("USER")
                                                .requestMatchers("/api/groups/**").hasRole("USER")
                                                .anyRequest().authenticated())
                                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                System.out.println("✅ Custom SecurityConfig loaded - Actuator and Swagger endpoints are public");
                return http.build();
        }
}