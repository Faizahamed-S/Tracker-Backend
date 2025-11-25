package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ApplicationStatus;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.boardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.*;

@Service
public class boardService {

    @Autowired
    boardRepository repo;

    @Autowired
    userService userService;

    /**
     * Get all applications for the currently authenticated user.
     * Only returns applications that belong to the current user.
     */
    public List<Application> getApplications() {
        User currentUser = userService.getCurrentUser();
        return repo.findByUser(currentUser);
    }

    /**
     * Get application by ID for the currently authenticated user.
     * Returns empty if application doesn't exist OR doesn't belong to current user.
     * This ensures users can only access their own applications.
     */
    public Optional<Application> getApplicationById(Long id) {
        User currentUser = userService.getCurrentUser();
        return repo.findByIdAndUser(id, currentUser);
    }

    /**
     * Create a new application for the currently authenticated user.
     * Automatically assigns the application to the current user, ignoring any user
     * field in the request.
     */
    public Application createApplication(Application application) {
        User currentUser = userService.getCurrentUser();
        // Auto-assign to current user - ignore any user field in request body
        application.setUser(currentUser);
        return repo.save(application);
    }

    /**
     * Update an application. The existing application must belong to the current
     * user.
     * Ownership is verified by getApplicationById() before calling this method.
     */
    public Application updateApplication(Application existing, Application newUpdate) {
        // Ensure user cannot be changed through update
        // (user is already set and verified via getApplicationById)

        if (newUpdate.getCompanyName() != null)
            existing.setCompanyName(newUpdate.getCompanyName());
        if (newUpdate.getRoleName() != null)
            existing.setRoleName(newUpdate.getRoleName());
        if (new Date().getTime() - existing.getDateOfApplication().getTime() < 60 * 1000)
            existing.setDateOfApplication(newUpdate.getDateOfApplication());
        if (newUpdate.getJobLink() != null)
            existing.setJobLink(newUpdate.getJobLink());
        if (newUpdate.getJobDescription() != null)
            existing.setJobDescription(newUpdate.getJobDescription());
        if (newUpdate.isReferral())
            existing.setReferral(true);
        if (newUpdate.isTailored())
            existing.setTailored(true);
        if (newUpdate.getStatus() != null)
            existing.setStatus(newUpdate.getStatus());
        return repo.save(existing);
    }

    /**
     * Delete an application by ID for the currently authenticated user.
     * Only deletes if the application belongs to the current user.
     * Returns true if deleted, false if not found or doesn't belong to user.
     */
    public boolean deleteApplication(Long id) {
        User currentUser = userService.getCurrentUser();
        Optional<Application> application = repo.findByIdAndUser(id, currentUser);
        if (application.isPresent()) {
            repo.delete(application.get());
            return true;
        }
        return false;
    }

    /**
     * Get applications by status for the currently authenticated user.
     * Only returns applications that belong to the current user and match the
     * status.
     */
    public List<Application> getApplicationByStatus(ApplicationStatus status) {
        User currentUser = userService.getCurrentUser();
        return repo.findByUserAndStatus(currentUser, status);
    }
}
