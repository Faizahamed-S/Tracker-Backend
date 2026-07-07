package com.ApplyZap.Tracker.service;

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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupServiceDeleteTest {

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
        group.setName("Test Group");
        group.setOwner(owner);

        ownerMember = new GroupMember();
        ownerMember.setId(10L);
        ownerMember.setGroup(group);
        ownerMember.setUser(owner);
        ownerMember.setRole(GroupRole.OWNER);

        when(userService.getCurrentUser()).thenReturn(owner);
        when(groupRepository.findById(2L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupAndUser(group, owner)).thenReturn(Optional.of(ownerMember));
    }

    @Test
    void deleteGroup_owner_callsCascadeAndDeleteById() {
        groupService.deleteGroup(2L);

        InOrder order = inOrder(
                groupJobStatusRepository,
                groupJobRepository,
                groupInviteRepository,
                groupMemberRepository,
                entityManager,
                groupRepository);
        order.verify(groupJobStatusRepository).deleteAllByGroupJobGroup(group);
        order.verify(groupJobRepository).deleteAllByGroup(group);
        order.verify(groupInviteRepository).deleteAllByGroup(group);
        order.verify(groupMemberRepository).deleteAllByGroup(group);
        order.verify(entityManager).flush();
        order.verify(entityManager).clear();
        order.verify(groupRepository).deleteById(2L);
    }

    @Test
    void deleteGroup_notOwner_throwsAndSkipsCascade() {
        ownerMember.setRole(GroupRole.MEMBER);

        assertThrows(SecurityException.class, () -> groupService.deleteGroup(2L));

        verify(groupJobStatusRepository, never()).deleteAllByGroupJobGroup(any());
        verify(groupRepository, never()).deleteById(any());
    }

    @Test
    void deleteGroupCascade_deletesChildrenThenGroupById() {
        groupService.deleteGroupCascade(2L);

        verify(groupRepository).deleteById(2L);
        verify(entityManager).flush();
        verify(entityManager).clear();
    }
}
