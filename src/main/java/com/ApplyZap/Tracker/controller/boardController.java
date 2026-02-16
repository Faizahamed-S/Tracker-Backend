package com.ApplyZap.Tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ApplyZap.Tracker.service.boardService;
import com.ApplyZap.Tracker.model.Application;
import java.util.*;

@CrossOrigin(origins = { "http://localhost:8081", "https://applyzap-auth-buddy.lovable.app",
        "https://2c784761dad8.ngrok-free.app", "chrome-extension://llhglfinjehpmcphdjkjnjgdogkkjbln" })
@RestController
@RequestMapping("/board")
public class boardController {

    @Autowired
    boardService boardService;

    @GetMapping("/applications")
    public ResponseEntity<List<Application>> getApplications() {
        return new ResponseEntity<>(boardService.getApplications(), HttpStatus.OK);
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        Optional<Application> application = boardService.getApplicationById(id);
        if (application.isPresent()) {
            return new ResponseEntity<>(application.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/applications")
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        return new ResponseEntity<>(boardService.createApplication(application), HttpStatus.CREATED);
    }

    @PutMapping("/applications/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable Long id, @RequestBody Application application) {
        Optional<Application> existing = boardService.getApplicationById(id);
        if (existing.isPresent()) {
            Application updated = boardService.updateApplication(existing.get(), application);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        // deleteApplication already verifies ownership and returns false if not found
        // or doesn't belong to user
        boolean deleted = boardService.deleteApplication(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            // Return 404 for security - don't reveal if application exists but belongs to
            // another user
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/applications/statuses")
    public ResponseEntity<List<String>> getUniqueStatuses() {
        return new ResponseEntity<>(boardService.getUniqueStatuses(), HttpStatus.OK);
    }

    @GetMapping("/applications/status/{status}")
    public ResponseEntity<List<Application>> getApplicationByStatus(@PathVariable String status) {
        return new ResponseEntity<>(boardService.getApplicationByStatus(status), HttpStatus.OK);
    }

    @PatchMapping("/applications/{id}")
    public ResponseEntity<Application> patchApplication(@PathVariable Long id, @RequestBody Application partialUpdate) {
        Optional<Application> existing = boardService.getApplicationById(id);
        if (existing.isPresent()) {
            Application updated = boardService.updateApplication(existing.get(), partialUpdate);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
