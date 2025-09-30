package com.ApplyZap.Tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ApplyZap.Tracker.service.boardService;
import com.ApplyZap.Tracker.model.Application;
import java.util.*;


@RestController
@RequestMapping("/board")
public class boardController {

    @Autowired
    boardService boardService;

    @GetMapping("/applications")
    public List<Application> getApplications(){
        return boardService.getApplications();
    }

    @GetMapping("/applications/{id}")
    public Application getApplicationById(@PathVariable Long id){
        return boardService.getApplicationById(id);
    }
}
