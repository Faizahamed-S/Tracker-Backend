package com.ApplyZap.Tracker.service;

import com.ApplyZap.Tracker.model.Application;
import com.ApplyZap.Tracker.model.ApplicationStatus;
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

    public Optional<Application> getApplicationById(Long id) {
        return repo.findById(id);
    }

    public Application createApplication(Application application) {
        return repo.save(application);
    }

    public Application updateApplication(Application existing, Application newUpdate) {
        if(newUpdate.getCompanyName()!=null)
            existing.setCompanyName(newUpdate.getCompanyName());
        if(newUpdate.getRoleName()!=null)
            existing.setRoleName(newUpdate.getRoleName());
        if(new Date().getTime()-existing.getDateOfApplication().getTime()<60*1000)
            existing.setDateOfApplication(newUpdate.getDateOfApplication());
        if(newUpdate.getJobLink()!=null)
            existing.setJobLink(newUpdate.getJobLink());
        if(newUpdate.getJobDescription()!=null)
            existing.setJobDescription(newUpdate.getJobDescription());
        if(newUpdate.isReferral())
            existing.setReferral(true);
        if(newUpdate.isTailored())
            existing.setTailored(true);

        return repo.save(existing);
    }

    public void deleteApplication(Long id) {
       repo.deleteById(id);
    }

    public List<Application> getApplicationByStatus(ApplicationStatus status) {
        return repo.findByStatus(status);
    }
}
