package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.dto.GroupCreateDTO;
import com.ApplyZap.Tracker.dto.GroupSummaryDTO;
import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupMember;
import com.ApplyZap.Tracker.model.GroupRole;
import com.ApplyZap.Tracker.model.User;
import com.ApplyZap.Tracker.repository.GroupInviteRepository;
import com.ApplyZap.Tracker.repository.GroupJobRepository;
import com.ApplyZap.Tracker.repository.GroupJobStatusRepository;
import com.ApplyZap.Tracker.repository.GroupMemberRepository;
import com.ApplyZap.Tracker.repository.GroupRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupServiceRenameTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private GroupJobRepository groupJobRepository;
    @Mock
    private GroupJobStatusRepository groupJobStatusRepository;
    @Mock
    private GroupInviteRepository groupInviteRepository;
    @Mock
    private userService userService;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private GroupService groupService;

    private User owner;
    private Group group;
    private GroupMember ownerMember;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);

        group = new Group();
        group.setId(2L);
        group.setName("Old Name");
        group.setOwner(owner);

        ownerMember = new GroupMember();
        ownerMember.setGroup(group);
        ownerMember.setUser(owner);
        ownerMember.setRole(GroupRole.OWNER);

        when(userService.getCurrentUser()).thenReturn(owner);
        when(groupRepository.findById(2L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupAndUser(group, owner)).thenReturn(Optional.of(ownerMember));
        when(groupRepository.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void updateGroupName_owner_savesTrimmedName() {
        GroupCreateDTO dto = new GroupCreateDTO();
        dto.setName("  New Name  ");

        GroupSummaryDTO result = groupService.updateGroupName(2L, dto);

        assertEquals("New Name", result.getName());
        verify(groupRepository).save(group);
    }

    @Test
    void updateGroupName_blankName_throws() {
        GroupCreateDTO dto = new GroupCreateDTO();
        dto.setName("   ");

        assertThrows(IllegalArgumentException.class, () -> groupService.updateGroupName(2L, dto));
        verify(groupRepository, never()).save(any());
    }

    @Test
    void updateGroupName_notOwner_throws() {
        ownerMember.setRole(GroupRole.MEMBER);
        GroupCreateDTO dto = new GroupCreateDTO();
        dto.setName("New Name");

        assertThrows(SecurityException.class, () -> groupService.updateGroupName(2L, dto));
        verify(groupRepository, never()).save(any());
    }
}
