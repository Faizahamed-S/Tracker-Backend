package com.ApplyZap.Tracker.controller;

import com.ApplyZap.Tracker.dto.GroupBoardDTO;
import com.ApplyZap.Tracker.dto.GroupJobCreateDTO;
import com.ApplyZap.Tracker.dto.GroupJobStatusUpdateDTO;
import com.ApplyZap.Tracker.dto.GroupJobUpdateDTO;
import com.ApplyZap.Tracker.model.GroupJob;
import com.ApplyZap.Tracker.service.GroupJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = { "http://localhost:8081", "https://applyzap-auth-buddy.lovable.app",
        "https://2c784761dad8.ngrok-free.app", "chrome-extension://llhglfinjehpmcphdjkjnjgdogkkjbln" })
@RestController
@RequestMapping("/api/groups/{groupId}/jobs")
@Tag(name = "Collaborative Group Jobs", description = "Job rows and status cells for a group board")
public class GroupJobController {

    @Autowired
    private GroupJobService groupJobService;

    @Operation(summary = "Get group board (members + jobs with statuses)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Board data"),
            @ApiResponse(responseCode = "403", description = "Not a member"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping
    public ResponseEntity<GroupBoardDTO> getBoard(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId) {
        return ResponseEntity.ok(groupJobService.getBoard(groupId));
    }

    @Operation(summary = "Add a job row (deduped by normalized URL)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Job created or existing returned"),
            @ApiResponse(responseCode = "400", description = "Invalid URL or missing link"),
            @ApiResponse(responseCode = "403", description = "Not a member"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PostMapping
    public ResponseEntity<GroupJob> createJob(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @RequestBody GroupJobCreateDTO dto) {
        GroupJob job = groupJobService.createJob(groupId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    @Operation(summary = "Update job metadata (link, company, role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "403", description = "Not a member"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PutMapping("/{jobId}")
    public ResponseEntity<Void> updateJob(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "Job ID", required = true) @PathVariable Long jobId,
            @RequestBody GroupJobUpdateDTO dto) {
        groupJobService.updateJob(groupId, jobId, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a job row")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Not a member"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "Job ID", required = true) @PathVariable Long jobId) {
        groupJobService.deleteJob(groupId, jobId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update my status cell for a job (Applied/Expired/N/A)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "403", description = "Not a member"),
            @ApiResponse(responseCode = "404", description = "Job not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PatchMapping("/{jobId}/status")
    public ResponseEntity<Void> updateMyStatus(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "Job ID", required = true) @PathVariable Long jobId,
            @RequestBody GroupJobStatusUpdateDTO dto) {
        groupJobService.updateMyStatus(groupId, jobId, dto);
        return ResponseEntity.ok().build();
    }
}
