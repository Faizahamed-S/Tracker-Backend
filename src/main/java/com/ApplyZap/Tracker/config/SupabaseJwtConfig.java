package com.ApplyZap.Tracker.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class SupabaseJwtConfig {

    @Value("${supabase.jwksUrl}")
    private String jwksUrl;

    @Value("${supabase.issuer}")
    private String issuer;

    @Bean
    public JWTVerifier jwtVerifier() {
        // Create JWKS provider
        JwkProvider jwkProvider = new UrlJwkProvider(jwksUrl);

        // Create RSAKeyProvider adapter
        RSAKeyProvider rsaKeyProvider = new RSAKeyProvider() {
            @Override
            public RSAPublicKey getPublicKeyById(String keyId) {
                try {
                    Jwk jwk = jwkProvider.get(keyId);
                    return (RSAPublicKey) jwk.getPublicKey();
                } catch (JwkException e) {
                    throw new RuntimeException("Failed to get public key for keyId: " + keyId, e);
                }
            }

            @Override
            public String getPrivateKeyId() {
                return null; // Not needed for verification
            }

            @Override
            public java.security.interfaces.RSAPrivateKey getPrivateKey() {
                return null; // Not needed for verification
            }
        };

        // Build JWT verifier with issuer validation and RS256 algorithm
        return JWT.require(Algorithm.RSA256(rsaKeyProvider))
                .withIssuer(issuer)
                .build();
    }
}
