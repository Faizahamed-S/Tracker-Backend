package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ApplicationStatus;
import com.ApplyZap.Tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface boardRepository extends JpaRepository<Application, Long> {

    // Find all applications for a specific user
    List<Application> findByUser(User user);

    // Find application by ID and user (ensures ownership)
    Optional<Application> findByIdAndUser(Long id, User user);

    // Find applications by user and status
    List<Application> findByUserAndStatus(User user, ApplicationStatus status);

    // Legacy method - kept for backward compatibility but should not be used
    // directly
    // Prefer findByUserAndStatus instead
    @Deprecated
    List<Application> findByStatus(ApplicationStatus status);
}
