package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.GroupDisplayNameDTO;
import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupMember;
import com.ApplyZap.Tracker.model.GroupRole;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.GroupMemberRepository;
import com.ApplyZap.Tracker.repository.GroupJobStatusRepository;
import com.ApplyZap.Tracker.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupMemberService {

    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private GroupJobStatusRepository groupJobStatusRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private userService userService;
    @Autowired
    private GroupService groupService;

    @Transactional
    public void leaveGroup(Long groupId) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new SecurityException("Not a member of this group"));
        if (member.getRole() == GroupRole.OWNER) {
            long count = groupMemberRepository.findByGroupOrderByJoinedAtAsc(group).size();
            if (count <= 1) {
                groupService.deleteGroupCascade(group.getId());
                return;
            }
            throw new IllegalStateException("Owner must transfer ownership or delete the group before leaving");
        }
        groupJobStatusRepository.deleteAllByMember(member);
        groupMemberRepository.delete(member);
        if (groupMemberRepository.findByGroupOrderByJoinedAtAsc(group).isEmpty()) {
            groupService.deleteGroup(groupId);
        }
    }

    @Transactional
    public void transferOwnership(Long groupId, Long newOwnerMemberId) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        groupService.requireOwner(group, currentUser);
        GroupMember currentOwner = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new SecurityException("Not a member"));
        GroupMember newOwner = groupMemberRepository.findByIdAndGroup(newOwnerMemberId, group)
                .orElseThrow(() -> new IllegalArgumentException("Member not found in this group"));
        if (newOwner.getId().equals(currentOwner.getId())) {
            return;
        }
        currentOwner.setRole(GroupRole.MEMBER);
        newOwner.setRole(GroupRole.OWNER);
        group.setOwner(newOwner.getUser());
        groupRepository.save(group);
        groupMemberRepository.save(currentOwner);
        groupMemberRepository.save(newOwner);
    }

    @Transactional
    public void updateMyDisplayName(Long groupId, GroupDisplayNameDTO dto) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new SecurityException("Not a member of this group"));
        if (dto.getDisplayName() != null && !dto.getDisplayName().isBlank()) {
            member.setDisplayName(dto.getDisplayName().trim());
            groupMemberRepository.save(member);
        }
    }

    @Transactional
    public void updateMemberDisplayName(Long groupId, Long memberId, GroupDisplayNameDTO dto) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        groupService.requireOwner(group, currentUser);
        GroupMember target = groupMemberRepository.findByIdAndGroup(memberId, group)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        if (dto.getDisplayName() != null && !dto.getDisplayName().isBlank()) {
            target.setDisplayName(dto.getDisplayName().trim());
            groupMemberRepository.save(target);
        }
    }

    @Transactional
    public void removeMember(Long groupId, Long memberId) {
        User currentUser = userService.getCurrentUser();
        Group group = groupService.getGroupEntity(groupId);
        groupService.requireOwner(group, currentUser);
        GroupMember target = groupMemberRepository.findByIdAndGroup(memberId, group)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        if (target.getRole() == GroupRole.OWNER) {
            throw new IllegalStateException("Cannot remove the owner. Transfer ownership first.");
        }
        groupJobStatusRepository.deleteAllByMember(target);
        groupMemberRepository.delete(target);
    }
}
