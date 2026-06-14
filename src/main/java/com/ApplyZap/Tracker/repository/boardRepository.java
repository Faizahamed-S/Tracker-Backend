package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;

@Repository
public interface boardRepository extends JpaRepository<Application, Long> {

    // Find all applications for a specific user
    @EntityGraph(attributePaths = "referralContact")
    List<Application> findByUser(User user);

    @EntityGraph(attributePaths = "referralContact")
    List<Application> findByUser(User user, Sort sort);

    @EntityGraph(attributePaths = "referralContact")
    List<Application> findByUserAndReferral(User user, boolean referral, Sort sort);

    @EntityGraph(attributePaths = "referralContact")
    List<Application> findByUserAndTailored(User user, boolean tailored, Sort sort);

    @EntityGraph(attributePaths = "referralContact")
    List<Application> findByUserAndReferralAndTailored(User user, boolean referral, boolean tailored, Sort sort);

    @EntityGraph(attributePaths = "referralContact")
    List<Application> findByUserAndReferral(User user, boolean referral);

    @EntityGraph(attributePaths = "referralContact")
    List<Application> findByUserAndTailored(User user, boolean tailored);

    @EntityGraph(attributePaths = "referralContact")
    List<Application> findByUserAndReferralAndTailored(User user, boolean referral, boolean tailored);

    long countByUserAndReferral(User user, boolean referral);

    long countByUserAndTailored(User user, boolean tailored);

    @Query("SELECT a FROM Application a WHERE a.createdAt IS NULL OR a.statusUpdatedAt IS NULL")
    List<Application> findNeedingTimestampBackfill();

    // Find application by ID and user (ensures ownership)
    @EntityGraph(attributePaths = "referralContact")
    Optional<Application> findByIdAndUser(Long id, User user);

    // Find applications by user and status
    List<Application> findByUserAndStatus(User user, String status);

    // Find applications by user and status (case-insensitive, for deployed compatibility)
    @Query("SELECT a FROM Application a WHERE a.user = :user AND UPPER(a.status) = UPPER(:status)")
    List<Application> findByUserAndStatusIgnoreCase(@Param("user") User user, @Param("status") String status);

    @Query("SELECT a FROM Application a WHERE a.user = :user AND UPPER(a.status) = UPPER(:status)")
    List<Application> findByUserAndStatusIgnoreCase(@Param("user") User user, @Param("status") String status, Sort sort);

    // Find distinct statuses for a user (for dynamic board columns)
    @Query("SELECT DISTINCT a.status FROM Application a WHERE a.user = :user AND a.status IS NOT NULL")
    List<String> findDistinctStatusesByUser(User user);

    // Count applications by status for a user (for analytics dashboard)
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.user = :user GROUP BY a.status")
    List<Object[]> countByUserGroupByStatus(@Param("user") User user);

    // Legacy method - kept for backward compatibility but should not be used
    // directly
    // Prefer findByUserAndStatus instead
    @Deprecated
    List<Application> findByStatus(String status);

    List<Application> findByUserAndReferralContact_Id(User user, Long referralContactId, Sort sort);
}
