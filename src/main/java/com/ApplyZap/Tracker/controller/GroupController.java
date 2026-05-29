package com.ApplyZap.Tracker.controller;

import com.ApplyZap.Tracker.dto.GroupCreateDTO;
import com.ApplyZap.Tracker.dto.GroupDisplayNameDTO;
import com.ApplyZap.Tracker.dto.GroupDTO;
import com.ApplyZap.Tracker.dto.GroupInviteDTO;
import com.ApplyZap.Tracker.dto.GroupInviteInfoDTO;
import com.ApplyZap.Tracker.dto.GroupSummaryDTO;
import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.service.GroupInviteService;
import com.ApplyZap.Tracker.service.GroupMemberService;
import com.ApplyZap.Tracker.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = { "http://localhost:8081", "https://applyzap-auth-buddy.lovable.app",
        "https://2c784761dad8.ngrok-free.app", "chrome-extension://llhglfinjehpmcphdjkjnjgdogkkjbln" })
@RestController
@RequestMapping("/api/groups")
@Tag(name = "Collaborative Groups", description = "Create and manage job tracker groups")
public class GroupController {

    @Autowired
    private GroupService groupService;
    @Autowired
    private GroupMemberService groupMemberService;
    @Autowired
    private GroupInviteService groupInviteService;

    @Operation(summary = "Create a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group created"),
            @ApiResponse(responseCode = "400", description = "Max groups reached or invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PostMapping
    public ResponseEntity<GroupSummaryDTO> createGroup(@RequestBody GroupCreateDTO dto) {
        Group group = groupService.createGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GroupSummaryDTO(group.getId(), group.getName(), group.getOwner().getId(), group.getCreatedAt()));
    }

    @Operation(summary = "List my groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of groups"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping
    public ResponseEntity<List<GroupSummaryDTO>> listGroups() {
        return ResponseEntity.ok(groupService.listMyGroups());
    }

    @Operation(summary = "Get group details and members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group details"),
            @ApiResponse(responseCode = "403", description = "Not a member"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId));
    }

    @Operation(summary = "Delete group (owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group deleted"),
            @ApiResponse(responseCode = "403", description = "Not owner"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Invite a user by email (owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invite created"),
            @ApiResponse(responseCode = "400", description = "Already a member or invalid email"),
            @ApiResponse(responseCode = "403", description = "Not owner"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PostMapping("/{groupId}/invites")
    public ResponseEntity<String> createInvite(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @RequestBody GroupInviteDTO dto) {
        var invite = groupInviteService.createInvite(groupId, dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(invite.getToken());
    }

    @Operation(summary = "Get invite details by token (for accept/decline page)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite info"),
            @ApiResponse(responseCode = "404", description = "Invite not found")
    })
    @GetMapping("/invites/{token}")
    public ResponseEntity<GroupInviteInfoDTO> getInviteInfo(
            @Parameter(description = "Invite token", required = true) @PathVariable String token) {
        return groupInviteService.getInviteInfo(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Accept an invite")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Joined group"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PostMapping("/invites/{token}/accept")
    public ResponseEntity<Void> acceptInvite(
            @Parameter(description = "Invite token", required = true) @PathVariable String token) {
        groupInviteService.acceptInvite(token);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Decline an invite")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite declined"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PostMapping("/invites/{token}/decline")
    public ResponseEntity<Void> declineInvite(
            @Parameter(description = "Invite token", required = true) @PathVariable String token) {
        groupInviteService.declineInvite(token);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Leave group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Left group"),
            @ApiResponse(responseCode = "400", description = "Owner must transfer first"),
            @ApiResponse(responseCode = "403", description = "Not a member"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @DeleteMapping("/{groupId}/members/me")
    public ResponseEntity<Void> leaveGroup(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId) {
        groupMemberService.leaveGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Transfer ownership to another member (owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ownership transferred"),
            @ApiResponse(responseCode = "403", description = "Not owner"),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PostMapping("/{groupId}/members/{memberId}/transfer-owner")
    public ResponseEntity<Void> transferOwnership(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "Member ID to become owner", required = true) @PathVariable Long memberId) {
        groupMemberService.transferOwnership(groupId, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update my display name in this group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "403", description = "Not a member"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PatchMapping("/{groupId}/members/me")
    public ResponseEntity<Void> updateMyDisplayName(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @RequestBody GroupDisplayNameDTO dto) {
        groupMemberService.updateMyDisplayName(groupId, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update a member's display name (owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "403", description = "Not owner"),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @PatchMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> updateMemberDisplayName(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "Member ID", required = true) @PathVariable Long memberId,
            @RequestBody GroupDisplayNameDTO dto) {
        groupMemberService.updateMemberDisplayName(groupId, memberId, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove a member from the group (owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member removed"),
            @ApiResponse(responseCode = "400", description = "Cannot remove owner"),
            @ApiResponse(responseCode = "403", description = "Not owner"),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "401", description = "Not authorized")
    })
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "Group ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "Member ID to remove", required = true) @PathVariable Long memberId) {
        groupMemberService.removeMember(groupId, memberId);
        return ResponseEntity.noContent().build();
    }
}
