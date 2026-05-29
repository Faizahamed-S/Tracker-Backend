package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.Group;
import com.ApplyZap.Tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g WHERE g IN (SELECT m.group FROM GroupMember m WHERE m.user = :user) ORDER BY g.createdAt DESC")
    List<Group> findByMemberUser(@Param("user") User user);

    long countByOwner(User owner);
}
