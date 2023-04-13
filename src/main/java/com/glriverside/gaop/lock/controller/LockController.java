package com.glriverside.gaop.lock.controller;

import com.glriverside.gaop.lock.LockManagerServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/lock")
@RestController
public class LockController {
    private LockManagerServiceImpl lockManager = new LockManagerServiceImpl();

    public LockController() {

    }

    @GetMapping("/{serviceName}")
    public void check(@PathVariable String serviceName) {
    }
}
