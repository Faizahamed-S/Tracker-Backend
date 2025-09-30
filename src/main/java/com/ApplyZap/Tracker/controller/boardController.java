package com.ApplyZap.Tracker.controller;

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
        Application application = boardService.getApplicationById(id);
        if(application == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else{
            return new ResponseEntity<>(application,HttpStatus.OK);
        }
    }
}
