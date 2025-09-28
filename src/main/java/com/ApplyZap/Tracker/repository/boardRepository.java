package com.ApplyZap.Tracker.repository;

import com.ApplyZap.Tracker.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface boardRepository extends JpaRepository<Application, Integer> {
}
