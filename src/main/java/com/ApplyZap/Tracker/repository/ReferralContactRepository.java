package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.ReferralContact;
import com.ApplyZap.Tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralContactRepository extends JpaRepository<ReferralContact, Long> {

    Optional<ReferralContact> findByIdAndUser(Long id, User user);

    List<ReferralContact> findByUserOrderByNameAsc(User user);

    @Query("SELECT r FROM ReferralContact r WHERE r.user = :user AND ("
            + "LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.companyName) LIKE LOWER(CONCAT('%', :q, '%'))) "
            + "ORDER BY r.name ASC")
    List<ReferralContact> searchByUser(@Param("user") User user, @Param("q") String q);
}
