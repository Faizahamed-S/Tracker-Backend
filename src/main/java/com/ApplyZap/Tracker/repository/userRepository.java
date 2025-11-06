package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface userRepository extends JpaRepository<User, Long> {

    // Find user by Supabase user ID (UUID from JWT token)
    Optional<User> findBySupabaseUserId(String supabaseUserId);

    // Find user by email (optional, for additional lookups)
    Optional<User> findByEmail(String email);
}
