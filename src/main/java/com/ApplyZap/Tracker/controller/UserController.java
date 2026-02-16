package com.ApplyZap.Tracker.controller;

import com.ApplyZap.Tracker.dto.UserProfileUpdateDTO;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.service.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = { "http://localhost:8081", "https://applyzap-auth-buddy.lovable.app",
        "https://2c784761dad8.ngrok-free.app", "chrome-extension://llhglfinjehpmcphdjkjnjgdogkkjbln" })
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private userService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        User currentUser = userService.getCurrentUser();
        String supabaseUserId = currentUser.getSupabaseUserId();
        Optional<User> user = userService.findBySupabaseUserId(supabaseUserId);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody UserProfileUpdateDTO dto) {
        User currentUser = userService.getCurrentUser();
        String supabaseUserId = currentUser.getSupabaseUserId();
        User updated = userService.updateUserProfile(supabaseUserId, dto);
        return ResponseEntity.ok(updated);
    }
}
