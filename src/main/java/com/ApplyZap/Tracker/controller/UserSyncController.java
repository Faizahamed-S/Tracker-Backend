package com.ApplyZap.Tracker.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserSyncController {

    @PostMapping("/user-sync")
    public ResponseEntity<Map<String, String>> syncUser(
            @RequestBody UserPayload user,
            @RequestHeader(value = "x-lovable-verified", required = false) String verifiedHeader) {

        // verify the request came from Lovable Cloud
        if (verifiedHeader == null || !"true".equalsIgnoreCase(verifiedHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unverified request source"));
        }

        // log what we received
        System.out.println("✅ Received verified user sync:");
        System.out.println("User ID: " + user.userId);
        System.out.println("Email: " + user.email);
        System.out.println("First Name: " + user.firstName);
        System.out.println("Last Name: " + user.lastName);
        System.out.println("Date of Birth: " + user.dateOfBirth);

        // optional: save to DB
        // userService.findOrCreateUser(user.userId, user.email, user.firstName, user.lastName, user.dateOfBirth);

        return ResponseEntity.ok(Map.of("message", "User synced successfully"));
    }

    // inner class for parsing JSON body
    public static class UserPayload {
        public String userId;
        public String email;
        public String firstName;
        public String lastName;
        public String dateOfBirth;
    }
}