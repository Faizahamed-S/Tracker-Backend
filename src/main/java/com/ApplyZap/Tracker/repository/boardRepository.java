package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface boardRepository extends JpaRepository<Application, Long> {
    List<Application> findByStatus(ApplicationStatus status);
}
