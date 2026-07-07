package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.GroupCreateDTO;
import com.ApplyZap.Tracker.dto.GroupDTO;
import com.ApplyZap.Tracker.dto.GroupMemberDTO;
import com.ApplyZap.Tracker.dto.GroupSummaryDTO;
import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupMember;
import com.ApplyZap.Tracker.model.GroupRole;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.GroupJobRepository;
import com.ApplyZap.Tracker.repository.GroupJobStatusRepository;
import com.ApplyZap.Tracker.repository.GroupMemberRepository;
import com.ApplyZap.Tracker.repository.GroupRepository;
import com.ApplyZap.Tracker.repository.GroupInviteRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Value("${collab.max-groups-per-user:2}")
    private int maxGroupsPerUser;

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private GroupJobRepository groupJobRepository;
    @Autowired
    private GroupJobStatusRepository groupJobStatusRepository;
    @Autowired
    private GroupInviteRepository groupInviteRepository;
    @Autowired
    private userService userService;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Group createGroup(GroupCreateDTO dto) {
        User currentUser = userService.getCurrentUser();
        long owned = groupRepository.countByOwner(currentUser);
        if (owned >= maxGroupsPerUser) {
            throw new IllegalStateException("Maximum number of groups (" + maxGroupsPerUser + ") reached");
        }
        Group group = new Group();
        group.setName(dto.getName() != null ? dto.getName().trim() : "Unnamed Group");
        group.setOwner(currentUser);
        group = groupRepository.save(group);
        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroup(group);
        ownerMember.setUser(currentUser);
        ownerMember.setDisplayName(currentUser.getFirstName() != null ? currentUser.getFirstName() : "Owner");
        ownerMember.setRole(GroupRole.OWNER);
        groupMemberRepository.save(ownerMember);
        return group;
    }

    public List<GroupSummaryDTO> listMyGroups() {
        User currentUser = userService.getCurrentUser();
        return groupRepository.findByMemberUser(currentUser).stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    public GroupDTO getGroup(Long groupId) {
        User currentUser = userService.getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new SecurityException("Not a member of this group"));
        List<GroupMemberDTO> members = groupMemberRepository.findByGroupOrderByJoinedAtAsc(group).stream()
                .map(m -> new GroupMemberDTO(m.getId(), m.getDisplayName(), m.getRole(), m.getUser().getId()))
                .collect(Collectors.toList());
        return new GroupDTO(group.getId(), group.getName(), group.getOwner().getId(), group.getCreatedAt(), members);
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        User currentUser = userService.getCurrentUser();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        requireOwner(group, currentUser);
        deleteGroupCascade(groupId);
    }

    /** Cascade delete group and all related data. Used by deleteGroup and when owner leaves as only member. */
    @Transactional
    public void deleteGroupCascade(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        groupJobStatusRepository.deleteAllByGroupJobGroup(group);
        groupJobRepository.deleteAllByGroup(group);
        groupInviteRepository.deleteAllByGroup(group);
        groupMemberRepository.deleteAllByGroup(group);
        entityManager.flush();
        entityManager.clear();
        groupRepository.deleteById(groupId);
    }

    public Group getGroupEntity(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    public void requireMembership(Group group, User user) {
        groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new SecurityException("Not a member of this group"));
    }

    public void requireOwner(Group group, User user) {
        GroupMember m = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new SecurityException("Not a member of this group"));
        if (m.getRole() != GroupRole.OWNER) {
            throw new SecurityException("Only the owner can perform this action");
        }
    }

    private GroupSummaryDTO toSummaryDTO(Group g) {
        return new GroupSummaryDTO(g.getId(), g.getName(), g.getOwner().getId(), g.getCreatedAt());
    }
}
