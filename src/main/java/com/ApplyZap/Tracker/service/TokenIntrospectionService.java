package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.config.SupabaseConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TokenIntrospectionService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SupabaseConfig supabaseConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Result class for token introspection
     */
    public static class TokenVerificationResult {
        private final boolean valid;
        private final String supabaseUserId;
        private final String email;
        private final String firstName;
        private final String lastName;
        private final LocalDate dateOfBirth;
        private final String errorMessage;

        private TokenVerificationResult(boolean valid, String supabaseUserId, String email,
                String firstName, String lastName, LocalDate dateOfBirth,
                String errorMessage) {
            this.valid = valid;
            this.supabaseUserId = supabaseUserId;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.dateOfBirth = dateOfBirth;
            this.errorMessage = errorMessage;
        }

        public static TokenVerificationResult success(String supabaseUserId, String email,
                String firstName, String lastName,
                LocalDate dateOfBirth) {
            return new TokenVerificationResult(true, supabaseUserId, email, firstName, lastName,
                    dateOfBirth, null);
        }

        public static TokenVerificationResult failure(String errorMessage) {
            return new TokenVerificationResult(false, null, null, null, null, null, errorMessage);
        }

        // Getters
        public boolean isValid() {
            return valid;
        }

        public String getSupabaseUserId() {
            return supabaseUserId;
        }

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Verify JWT token by calling Supabase's /auth/v1/user endpoint.
     * If Supabase returns user data, the token is valid.
     */
    public TokenVerificationResult verifyToken(String token) {
        String authUrl = supabaseConfig.getAuthUrl();
        System.out.println("DEBUG - Auth URL: " + authUrl);
        System.out.println("DEBUG - Anon Key: " + (supabaseConfig.getAnonKey() != null ? "EXISTS" : "NULL/MISSING")); // adding
                                                                                                                      // debug
                                                                                                                      // for
                                                                                                                      // local
                                                                                                                      // dev
                                                                                                                      // testing
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("apikey", supabaseConfig.getAnonKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            System.out.println("🔍 TokenIntrospectionService: Calling Supabase API: " + authUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                    authUrl,
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("✅ TokenIntrospectionService: Supabase API returned 200 OK");

                JsonNode userData = objectMapper.readTree(response.getBody());

                // Extract required fields
                String supabaseUserId = userData.has("id") ? userData.get("id").asText() : null;
                String email = userData.has("email") ? userData.get("email").asText() : null;

                // Validate essential fields
                if (supabaseUserId == null || email == null) {
                    return TokenVerificationResult.failure("Invalid response: missing required fields (id or email)");
                }

                // Extract user_metadata
                String firstName = null;
                String lastName = null;
                LocalDate dateOfBirth = null;

                if (userData.has("user_metadata") && userData.get("user_metadata").isObject()) {
                    JsonNode userMetadata = userData.get("user_metadata");

                    if (userMetadata.has("first_name")) {
                        firstName = userMetadata.get("first_name").asText(null);
                    }
                    if (userMetadata.has("last_name")) {
                        lastName = userMetadata.get("last_name").asText(null);
                    }
                    if (userMetadata.has("dob")) {
                        String dobStr = userMetadata.get("dob").asText(null);
                        if (dobStr != null && !dobStr.isEmpty()) {
                            try {
                                dateOfBirth = LocalDate.parse(dobStr, DateTimeFormatter.ISO_LOCAL_DATE);
                            } catch (Exception e) {
                                System.out.println("⚠️ TokenIntrospectionService: Failed to parse dob: " + dobStr);
                                // Continue without dateOfBirth
                            }
                        }
                    }
                }

                System.out.println("✅ TokenIntrospectionService: Token verified - userId: " + supabaseUserId
                        + ", email: " + email);

                return TokenVerificationResult.success(supabaseUserId, email, firstName, lastName, dateOfBirth);

            } else {
                return TokenVerificationResult.failure("Invalid response from Supabase API");
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            System.out.println("❌ TokenIntrospectionService: Unauthorized (401) - token is invalid or expired");
            return TokenVerificationResult.failure("Invalid or expired token");
        } catch (HttpClientErrorException e) {
            System.out.println("❌ TokenIntrospectionService: HTTP error " + e.getStatusCode() + ": " + e.getMessage());
            return TokenVerificationResult.failure("Supabase API error: " + e.getStatusCode() + " - " + e.getMessage());
        } catch (RestClientException e) {
            System.out.println("❌ TokenIntrospectionService: Network error: " + e.getMessage());
            return TokenVerificationResult.failure("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ TokenIntrospectionService: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return TokenVerificationResult.failure("Error verifying token: " + e.getMessage());
        }
    }
}
