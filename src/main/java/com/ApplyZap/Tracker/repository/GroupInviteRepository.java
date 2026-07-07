package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupInvite;
import com.ApplyZap.Tracker.model.GroupInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {

    Optional<GroupInvite> findByToken(String token);

    List<GroupInvite> findByGroupAndStatus(Group group, GroupInviteStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM GroupInvite i WHERE i.group = :group")
    void deleteAllByGroup(@Param("group") Group group);

    @Query("SELECT i FROM GroupInvite i WHERE i.token = :token AND i.status = 'PENDING' AND i.expiresAt > :now")
    Optional<GroupInvite> findValidByToken(@Param("token") String token, @Param("now") LocalDateTime now);
}
