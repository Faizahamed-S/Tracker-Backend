package com.ApplyZap.Tracker.controller;

import com.ApplyZap.Tracker.dto.GroupAnalyticsDTO;
import com.ApplyZap.Tracker.service.GroupAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = { "http://localhost:8081", "https://applyzap-auth-buddy.lovable.app",
        "https://2c784761dad8.ngrok-free.app", "chrome-extension://llhglfinjehpmcphdjkjnjgdogkkjbln" })
@RestController
@RequestMapping("/api/groups/{groupId}/analytics")
@Tag(name = "Collaborative Group Analytics", description = "Group-level analytics for the collaborative job tracker")
public class GroupAnalyticsController {

    @Autowired
    private GroupAnalyticsService groupAnalyticsService;

    @Operation(summary = "Get group analytics (total jobs, added today, top contributors overall and by day)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics data"),
            @ApiResponse(responseCode = "403", description = "Not a member"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping
    public ResponseEntity<GroupAnalyticsDTO> getAnalytics(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "Date for 'biggest contributor by day' (default: today)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(groupAnalyticsService.getAnalytics(groupId, date));
    }
}
