package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.repository.boardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.*;

@Service
public class boardService {

    @Autowired
    boardRepository repo;

    public List<Application> getApplications() {
        return repo.findAll();
    }

    public Application getApplicationById(Long id) {
        return repo.findById(id).orElse(new Application());
    }

    public Application createApplication(Application application) {
        return repo.save(application);
    }
}
