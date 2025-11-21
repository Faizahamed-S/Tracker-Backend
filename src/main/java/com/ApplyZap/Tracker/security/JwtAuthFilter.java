package com.ApplyZap.Tracker.security;

import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.service.userService;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JWTVerifier jwtVerifier;

    @Autowired
    private userService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Verify token using Supabase public key / JWKS
            DecodedJWT decodedJWT = jwtVerifier.verify(token);

            // Extract claims
            String supabaseUserId = decodedJWT.getSubject(); // "sub" claim
            String email = decodedJWT.getClaim("email").asString();

            // Extract user_metadata from JWT
            Map<String, Object> userMetadata = decodedJWT.getClaim("user_metadata").asMap();
            String firstName = null;
            String lastName = null;
            LocalDate dateOfBirth = null;

            if (userMetadata != null) {
                firstName = (String) userMetadata.get("first_name");
                lastName = (String) userMetadata.get("last_name");

                Object dobObj = userMetadata.get("date_of_birth");
                if (dobObj instanceof String) {
                    try {
                        dateOfBirth = LocalDate.parse((String) dobObj, DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (Exception e) {
                        logger.warn("Failed to parse date_of_birth: " + dobObj, e);
                    }
                }
            }

            // Validate essential claims
            if (supabaseUserId == null || email == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid token: missing required claims\"}");
                return;
            }

            // Find or create the user record
            User user = userService.findOrCreateUser(
                    supabaseUserId,
                    email,
                    firstName,
                    lastName,
                    dateOfBirth);

            // Attach user to request for downstream usage
            request.setAttribute("user", user);

            // Continue the chain
            filterChain.doFilter(request, response);

        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error processing JWT token", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/api/user-sync")
                || path.startsWith("/api/user-sync/test");
    }
}