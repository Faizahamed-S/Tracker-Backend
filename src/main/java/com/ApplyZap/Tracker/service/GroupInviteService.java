package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupInvite;
import com.ApplyZap.Tracker.model.GroupInviteStatus;
import com.ApplyZap.Tracker.dto.GroupInviteInfoDTO;
import com.ApplyZap.Tracker.model.GroupMember;
import com.ApplyZap.Tracker.model.GroupRole;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.GroupInviteRepository;
import com.ApplyZap.Tracker.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class GroupInviteService {

    @Value("${collab.invite-expiry-hours:168}") // 7 days default
    private int inviteExpiryHours;

    @Autowired
    private GroupInviteRepository groupInviteRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private userService userService;
    @Autowired
    private GroupService groupService;

    @Transactional
    public GroupInvite createInvite(Long groupId, String email) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        groupService.requireOwner(group, currentUser);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (groupMemberRepository.findByGroupOrderByJoinedAtAsc(group).stream()
                .anyMatch(m -> m.getUser().getEmail().equalsIgnoreCase(normalizedEmail))) {
            throw new IllegalStateException("User is already a member");
        }
        Optional<GroupInvite> existing = groupInviteRepository.findByGroupAndStatus(group, GroupInviteStatus.PENDING).stream()
                .filter(i -> i.getEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }
        GroupInvite invite = new GroupInvite();
        invite.setGroup(group);
        invite.setEmail(normalizedEmail);
        invite.setInvitedBy(currentUser);
        invite.setStatus(GroupInviteStatus.PENDING);
        invite.setToken(UUID.randomUUID().toString().replace("-", ""));
        invite.setExpiresAt(LocalDateTime.now().plusHours(inviteExpiryHours));
        return groupInviteRepository.save(invite);
    }

    @Transactional
    public void acceptInvite(String token) {
        User currentUser = userService.getCurrentUser();
        GroupInvite invite = groupInviteRepository.findValidByToken(token, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invite"));
        if (!invite.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new SecurityException("This invite was sent to a different email address");
        }
        Group group = invite.getGroup();
        if (groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            invite.setStatus(GroupInviteStatus.ACCEPTED);
            groupInviteRepository.save(invite);
            return;
        }
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(currentUser);
        member.setDisplayName(currentUser.getFirstName() != null ? currentUser.getFirstName() : "Member");
        member.setRole(GroupRole.MEMBER);
        groupMemberRepository.save(member);
        invite.setStatus(GroupInviteStatus.ACCEPTED);
        groupInviteRepository.save(invite);
    }

    @Transactional
    public void declineInvite(String token) {
        GroupInvite invite = groupInviteRepository.findValidByToken(token, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invite"));
        invite.setStatus(GroupInviteStatus.DECLINED);
        groupInviteRepository.save(invite);
    }

    public Optional<GroupInvite> getInviteByToken(String token) {
        return groupInviteRepository.findByToken(token);
    }

    public Optional<GroupInviteInfoDTO> getInviteInfo(String token) {
        return groupInviteRepository.findByToken(token)
                .map(inv -> {
                    boolean valid = inv.getStatus() == GroupInviteStatus.PENDING
                            && inv.getExpiresAt().isAfter(LocalDateTime.now());
                    String inviterName = inv.getInvitedBy().getFirstName() + " " + inv.getInvitedBy().getLastName();
                    return new GroupInviteInfoDTO(inv.getGroup().getName(), inviterName, inv.getEmail(), valid);
                });
    }
}
