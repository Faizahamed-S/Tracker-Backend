package com.ApplyZap.Tracker.controller;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ApplyZap.Tracker.service.boardService;
import com.ApplyZap.Tracker.model.Application;
import java.util.*;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/board")
public class boardController {

    @Autowired
    boardService boardService;

    @GetMapping("/applications")
    public ResponseEntity<List<Application>> getApplications(){
        return new ResponseEntity<>(boardService.getApplications(),HttpStatus.OK);
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id){
            Optional<Application> application = boardService.getApplicationById(id);
            if(application.isPresent()){
            return new ResponseEntity<>(application.get(),HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
    }
    @PostMapping("/application")
    public ResponseEntity<Application> createApplication(@RequestBody Application application){
        return new ResponseEntity<>(boardService.createApplication(application),HttpStatus.CREATED);
    }

    @PutMapping("/application/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable Long id, @RequestBody Application application)
    {
        Optional<Application> existing= boardService.getApplicationById(id);
        if(existing.isPresent()){
            Application updated=boardService.updateApplication(existing.get(), application);
            return new ResponseEntity<>(updated,HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/application/{id}")
    public ResponseEntity<Application> deleteApplication(@PathVariable Long id){
        Optional<Application> application = boardService.getApplicationById(id);
        if(application.isPresent())
        {
            boardService.deleteApplication(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/applications/{@}")
}
