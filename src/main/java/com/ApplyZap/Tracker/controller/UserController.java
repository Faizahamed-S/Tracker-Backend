package com.ApplyZap.Tracker.controller;

import com.ApplyZap.Tracker.dto.ProfileReadinessDTO;
import com.ApplyZap.Tracker.dto.UserProfileUpdateDTO;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.service.ProfileReadinessService;
import com.ApplyZap.Tracker.service.userService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = { "http://localhost:8081", "https://applyzap-auth-buddy.lovable.app",
        "https://2c784761dad8.ngrok-free.app", "chrome-extension://llhglfinjehpmcphdjkjnjgdogkkjbln" })
@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Manage user profile information")
public class UserController {

    @Autowired
    private userService userService;

    @Autowired
    private ProfileReadinessService profileReadinessService;

    @Operation(summary = "Get user profile", description = "Get the current user's profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        User currentUser = userService.getCurrentUser();
        String supabaseUserId = currentUser.getSupabaseUserId();
        Optional<User> user = userService.findBySupabaseUserId(supabaseUserId);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Profile readiness", description = "Whether the profile has enough data for resume generation "
            + "and for application-form autofill. 'missing' lists JSON paths; dates set but not YYYY-MM appear as "
            + "'<path> (format)'. missing.forms lists only the extras beyond resume.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Readiness computed"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping("/profile/readiness")
    public ResponseEntity<ProfileReadinessDTO> getReadiness() {
        return ResponseEntity.ok(profileReadinessService.getForCurrentUser());
    }

    @Operation(summary = "Update user profile", description = "Update the current user's profile. "
            + "profileData is merged by top-level key: a key with a value replaces it, a key with null removes it, "
            + "absent keys are kept. Send profileData.schemaVersion = 2 for strict v2 validation (not stored). "
            + "Missing ids on experiences/projects/education/highlights are assigned by the server.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid profileData (body has message and field path)"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody UserProfileUpdateDTO dto) {
        User currentUser = userService.getCurrentUser();
        String supabaseUserId = currentUser.getSupabaseUserId();
        User updated = userService.updateUserProfile(supabaseUserId, dto);
        return ResponseEntity.ok(updated);
    }
}
