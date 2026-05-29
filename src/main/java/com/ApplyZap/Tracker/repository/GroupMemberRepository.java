package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupMember;
import com.ApplyZap.Tracker.model.GroupRole;
import com.ApplyZap.Tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupOrderByJoinedAtAsc(Group group);

    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    boolean existsByGroupAndUser(Group group, User user);

    Optional<GroupMember> findByIdAndGroup(Long id, Group group);

    @Query("SELECT m FROM GroupMember m WHERE m.group = :group AND m.role = :role")
    Optional<GroupMember> findByGroupAndRole(@Param("group") Group group, @Param("role") GroupRole role);

    @Modifying
    @Query("DELETE FROM GroupMember m WHERE m.group = :group")
    void deleteAllByGroup(@Param("group") Group group);
}
