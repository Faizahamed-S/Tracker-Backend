package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.GroupJob;
import com.ApplyZap.Tracker.model.GroupJobStatus;
import com.ApplyZap.Tracker.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupJobStatusRepository extends JpaRepository<GroupJobStatus, Long> {

    List<GroupJobStatus> findByGroupJob(GroupJob groupJob);

    Optional<GroupJobStatus> findByGroupJobAndMember(GroupJob groupJob, GroupMember member);

    @Modifying
    @Query("DELETE FROM GroupJobStatus s WHERE s.member = :member")
    void deleteAllByMember(@Param("member") GroupMember member);

    @Modifying
    @Query("DELETE FROM GroupJobStatus s WHERE s.groupJob IN (SELECT j FROM GroupJob j WHERE j.group = :group)")
    void deleteAllByGroupJobGroup(@Param("group") Group group);
}
