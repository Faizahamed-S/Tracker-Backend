package com.ApplyZap.Tracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Board", description = "Manage job applications on your kanban board")
public class boardController {

    @Autowired
    boardService boardService;

    @Operation(summary = "Get all applications", description = "Get all job applications for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping("/applications")
    public ResponseEntity<List<Application>> getApplications() {
        return new ResponseEntity<>(boardService.getApplications(), HttpStatus.OK);
    }

    @Operation(summary = "Get application by ID", description = "Get a single job application by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application found"),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping("/applications/{id}")
    public ResponseEntity<Application> getApplicationById(
            @Parameter(description = "Application ID", required = true, example = "1")
            @PathVariable Long id) {
        Optional<Application> application = boardService.getApplicationById(id);
        if (application.isPresent()) {
            return new ResponseEntity<>(application.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Create new application", description = "Add a new job application to your board")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application created"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PostMapping("/applications")
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        return new ResponseEntity<>(boardService.createApplication(application), HttpStatus.CREATED);
    }

    @Operation(summary = "Update application", description = "Update all fields of an existing job application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application updated"),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PutMapping("/applications/{id}")
    public ResponseEntity<Application> updateApplication(
            @Parameter(description = "Application ID", required = true, example = "1")
            @PathVariable Long id, 
            @RequestBody Application application) {
        Optional<Application> existing = boardService.getApplicationById(id);
        if (existing.isPresent()) {
            Application updated = boardService.updateApplication(existing.get(), application);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete application", description = "Delete a job application from your board")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application deleted"),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Void> deleteApplication(
            @Parameter(description = "Application ID", required = true, example = "1")
            @PathVariable Long id) {
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

    @Operation(summary = "Get all statuses", description = "Get list of all unique application statuses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping("/applications/statuses")
    public ResponseEntity<List<String>> getUniqueStatuses() {
        return new ResponseEntity<>(boardService.getUniqueStatuses(), HttpStatus.OK);
    }

    @Operation(summary = "Get applications by status", description = "Get all applications filtered by a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping("/applications/status/{status}")
    public ResponseEntity<List<Application>> getApplicationByStatus(
            @Parameter(description = "Status name (e.g., 'applied', 'interview', 'offer')", required = true, example = "applied")
            @PathVariable String status) {
        return new ResponseEntity<>(boardService.getApplicationByStatus(status), HttpStatus.OK);
    }

    @Operation(summary = "Partially update application", description = "Update only specific fields of an application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application updated"),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PatchMapping("/applications/{id}")
    public ResponseEntity<Application> patchApplication(
            @Parameter(description = "Application ID", required = true, example = "1")
            @PathVariable Long id, 
            @RequestBody Application partialUpdate) {
        Optional<Application> existing = boardService.getApplicationById(id);
        if (existing.isPresent()) {
            Application updated = boardService.updateApplication(existing.get(), partialUpdate);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
