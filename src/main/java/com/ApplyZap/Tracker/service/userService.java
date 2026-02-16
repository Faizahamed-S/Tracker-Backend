package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.UserProfileUpdateDTO;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
        // Default tracker config (original board): Wishlist, Applied, Interviewing, Offer, Rejected
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("columns", List.of(
                Map.of("id", "col_wishlist", "title", "Wishlist", "color", "gray"),
                Map.of("id", "col_applied", "title", "Applied", "color", "blue"),
                Map.of("id", "col_interview", "title", "Interviewing", "color", "yellow"),
                Map.of("id", "col_offer", "title", "Offer", "color", "green"),
                Map.of("id", "col_rejected", "title", "Rejected", "color", "red")
        ));
        newUser.setTrackerConfig(defaultConfig);
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

    /**
     * Update user profile by supabase user ID. Partial update: only non-null DTO
     * fields are applied. profileData overwrites the existing map when non-null.
     *
     * @param supabaseUserId UUID from Supabase JWT token
     * @param dto             profile fields to update
     * @return updated User entity
     * @throws RuntimeException if user not found
     */
    @Transactional
    public User updateUserProfile(String supabaseUserId, UserProfileUpdateDTO dto) {
        User user = userRepository.findBySupabaseUserId(supabaseUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + supabaseUserId));
        if (dto.getFirstName() != null)
            user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            user.setLastName(dto.getLastName());
        if (dto.getTimezone() != null)
            user.setTimezone(dto.getTimezone());
        if (dto.getProfileData() != null)
            user.setProfileData(dto.getProfileData());
        if (dto.getTrackerConfig() != null)
            user.setTrackerConfig(dto.getTrackerConfig());
        return userRepository.save(user);
    }
}
