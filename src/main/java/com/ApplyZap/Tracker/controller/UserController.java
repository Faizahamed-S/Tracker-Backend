package com.ApplyZap.Tracker.controller;

import com.ApplyZap.Tracker.dto.UserProfileUpdateDTO;
import com.ApplyZap.Tracker.model.User;
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

    @Operation(summary = "Update user profile", description = "Update the current user's profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated"),
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
