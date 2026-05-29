package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupJob;
import com.ApplyZap.Tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupJobRepository extends JpaRepository<GroupJob, Long> {

    List<GroupJob> findByGroupOrderByCreatedAtDesc(Group group);

    Optional<GroupJob> findByIdAndGroup(Long id, Group group);

    Optional<GroupJob> findByGroupAndNormalizedUrl(Group group, String normalizedUrl);

    @Modifying
    @Query("DELETE FROM GroupJob j WHERE j.group = :group")
    void deleteAllByGroup(@Param("group") Group group);

    @Query("SELECT COUNT(j) FROM GroupJob j WHERE j.group = :group")
    long countByGroup(@Param("group") Group group);

    @Query(value = "SELECT COUNT(*) FROM group_job j WHERE j.group_id = :groupId AND CAST(j.created_at AS date) = CAST(CURRENT_TIMESTAMP AS date)", nativeQuery = true)
    long countByGroupAndCreatedAtToday(@Param("groupId") Long groupId);
}
