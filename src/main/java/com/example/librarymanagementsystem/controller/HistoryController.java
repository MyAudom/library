package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    @Autowired
    private HistoryService historyService;

    @GetMapping("/weekly")
    public List<HistoryService.DailyActivity> getWeeklyActivity() {
        return historyService.getWeeklyActivity();
    }
}
