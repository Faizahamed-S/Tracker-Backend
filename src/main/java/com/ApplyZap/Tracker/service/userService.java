package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class userService {

    @Autowired
    private userRepository userRepository;

    /**
     * Find or create user based on Supabase user ID.
     * If user exists, return it. If not, create a new record from token claims.
     * 
     * @param supabaseUserId UUID from Supabase JWT token
     * @param email          Email from token claims
     * @param firstName      First name from token claims
     * @param lastName       Last name from token claims
     * @param dateOfBirth    Date of birth from token claims (can be null)
     * @return User entity (existing or newly created)
     */
    @Transactional
    public User findOrCreateUser(String supabaseUserId, String email,
            String firstName, String lastName,
            LocalDate dateOfBirth) {

        // Check if user already exists
        Optional<User> existingUser = userRepository.findBySupabaseUserId(supabaseUserId);

        if (existingUser.isPresent()) {
            // User exists, return it
            return existingUser.get();
        }

        // User doesn't exist, create new record
        User newUser = new User();
        newUser.setSupabaseUserId(supabaseUserId);
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setDateOfBirth(dateOfBirth);
        // createdAt and updatedAt will be set automatically by @PrePersist

        return userRepository.save(newUser);
    }

    /**
     * Find user by Supabase user ID (for later use in controllers/services)
     */
    public Optional<User> findBySupabaseUserId(String supabaseUserId) {
        return userRepository.findBySupabaseUserId(supabaseUserId);
    }

    /**
     * Get the currently authenticated user from SecurityContext.
     * Returns the User object that was set during authentication in JwtAuthFilter.
     * 
     * @return Current authenticated User
     * @throws IllegalStateException if no authenticated user is found
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        throw new IllegalStateException("No authenticated user found in SecurityContext");
    }
}
