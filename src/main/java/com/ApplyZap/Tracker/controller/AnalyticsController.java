package com.ApplyZap.Tracker.controller;

import com.ApplyZap.Tracker.dto.DashboardDTO;
import com.ApplyZap.Tracker.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = { "http://localhost:8081", "https://applyzap-auth-buddy.lovable.app",
        "https://2c784761dad8.ngrok-free.app", "chrome-extension://llhglfinjehpmcphdjkjnjgdogkkjbln" })
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Dashboard and analytics for job applications")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Operation(summary = "Get analytics dashboard", description = "Returns summary counts (total applications, interviews, offers) and recent activity by date for the last 7 days")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data returned successfully"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboard());
    }
}
