package com.ApplyZap.Tracker.security;

import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.service.TokenIntrospectionService;
import com.ApplyZap.Tracker.service.userService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private TokenIntrospectionService tokenIntrospectionService;

    @Autowired
    private userService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Force output with System.out.println to ensure we see it
        System.out.println("🔍 JwtAuthFilter: Processing request to " + request.getRequestURI());

        // Extract token from Authorization header (case-insensitive lookup)
        // Headers can be lowercased by proxies/ngrok/browsers
        // Try lowercase FIRST since ngrok/browsers send it that way
        String authHeader = getAuthorizationHeader(request);

        System.out.println("🔍 JwtAuthFilter: Authorization header = "
                + (authHeader != null ? "FOUND (length: " + authHeader.length() + ")" : "NULL"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Reduce log noise for common browser requests without auth
            String path = request.getRequestURI();
            boolean isPublicPath = path.equals("/") || path.equals("/favicon.ico") ||
                    path.startsWith("/actuator") || path.startsWith("/api/user-sync");

            if (!isPublicPath) {
                // Only log errors for API endpoints that should have auth
                System.out.println(
                        "❌ JwtAuthFilter: Missing or invalid Authorization header for: " + path);
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }

        System.out.println("✅ JwtAuthFilter: Found valid Authorization header, proceeding with token introspection...");

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        System.out.println("✅ JwtAuthFilter: Extracted token (length: " + token.length() + ")");

        try {
            // Verify token via Supabase API (token introspection)
            // This calls Supabase's /auth/v1/user endpoint which validates the token
            // server-side
            System.out.println("🔍 JwtAuthFilter: Verifying token via Supabase API...");
            TokenIntrospectionService.TokenVerificationResult result = tokenIntrospectionService.verifyToken(token);

            if (!result.isValid()) {
                System.out.println("❌ JwtAuthFilter: Token verification failed: " + result.getErrorMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"" + result.getErrorMessage() + "\"}");
                return;
            }

            System.out.println("✅ JwtAuthFilter: Token verified successfully via Supabase API");

            // Extract user data from token introspection result
            String supabaseUserId = result.getSupabaseUserId();
            String email = result.getEmail();
            String firstName = result.getFirstName();
            String lastName = result.getLastName();
            LocalDate dateOfBirth = result.getDateOfBirth();

            System.out
                    .println("✅ JwtAuthFilter: Extracted user data - userId: " + supabaseUserId + ", email: " + email);

            // Provide default values for firstName/lastName if null (User entity has
            // nullable = false)
            if (firstName == null || firstName.trim().isEmpty()) {
                firstName = "User";
                System.out.println("⚠️ JwtAuthFilter: firstName was null, using default: 'User'");
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                lastName = "Name";
                System.out.println("⚠️ JwtAuthFilter: lastName was null, using default: 'Name'");
            }

            // Find or create the user record
            System.out.println("🔍 JwtAuthFilter: Finding or creating user...");
            User user = userService.findOrCreateUser(
                    supabaseUserId,
                    email,
                    firstName,
                    lastName,
                    dateOfBirth);
            System.out.println("✅ JwtAuthFilter: User found/created - ID: " + user.getId());

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            System.out.println("✅ JwtAuthFilter: Created authorities: ROLE_USER");

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
            System.out.println("✅ JwtAuthFilter: Created Authentication token");

            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("✅ JwtAuthFilter: Set Authentication in SecurityContext");

            // Attach user to request for downstream usage (optional, for backwards
            // compatibility)
            request.setAttribute("user", user);
            System.out.println("✅ JwtAuthFilter: Attached user to request attribute");

            // Continue the chain
            System.out.println("✅ JwtAuthFilter: Continuing filter chain...");
            filterChain.doFilter(request, response);
            System.out.println("✅ JwtAuthFilter: Filter chain completed successfully");

        } catch (Exception e) {
            System.out.println("❌ JwtAuthFilter: Exception during token processing: " + e.getMessage());
            e.printStackTrace();
            logger.error("Error processing token", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    /**
     * Case-insensitive lookup for Authorization header.
     * Headers can be lowercased by proxies (ngrok), browsers, or other
     * intermediaries.
     * 
     * Tries lowercase FIRST since ngrok/proxies commonly send it that way.
     */
    private String getAuthorizationHeader(HttpServletRequest request) {
        // Try lowercase FIRST (common with ngrok/proxies - this is what we're seeing in
        // logs)
        String header = request.getHeader("authorization");
        System.out.println("  🔍 getHeader('authorization'): " + (header != null ? "FOUND" : "NULL"));
        if (header != null) {
            System.out.println("  ✅ Found header via 'authorization' (lowercase)");
            return header;
        }

        // Try standard case (most common in direct requests)
        header = request.getHeader("Authorization");
        System.out.println("  🔍 getHeader('Authorization'): " + (header != null ? "FOUND" : "NULL"));
        if (header != null) {
            System.out.println("  ✅ Found header via 'Authorization' (standard case)");
            return header;
        }

        // Try uppercase
        header = request.getHeader("AUTHORIZATION");
        System.out.println("  🔍 getHeader('AUTHORIZATION'): " + (header != null ? "FOUND" : "NULL"));
        if (header != null) {
            System.out.println("  ✅ Found header via 'AUTHORIZATION' (uppercase)");
            return header;
        }

        // Fallback: search through all headers case-insensitively
        System.out.println("  🔍 Direct lookups failed, searching through all headers...");
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            int headerCount = 0;
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headerCount++;
                System.out.println("    Checking Header[" + headerCount + "]: " + headerName);
                if ("authorization".equalsIgnoreCase(headerName)) {
                    String value = request.getHeader(headerName);
                    System.out.println("    ✅ MATCH FOUND! Header name: '" + headerName + "', value length: "
                            + (value != null ? value.length() : 0));
                    return value;
                }
            }
            System.out.println("    ❌ Searched through " + headerCount
                    + " headers but did not find 'authorization' (case-insensitive)");
        } else {
            System.out.println("    ❌ request.getHeaderNames() returned NULL!");
        }

        System.out.println("  ❌ Authorization header not found in any form");
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Let CORS preflight (OPTIONS) pass without auth - browser doesn't send Authorization on preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        // Skip filter for public paths (no auth required)
        return path.equals("/")
                || path.equals("/favicon.ico")
                || path.startsWith("/actuator")
                || path.startsWith("/api/user-sync")
                || path.startsWith("/api/user-sync/test")
                || path.startsWith("/h2-console")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}