package com.glriverside.gaop.lock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class LockController {
    private LockManager lockManager = new LockManager();

    public LockController() {

    }

    @GetMapping("/{serviceName}")
    public void check(@PathVariable String serviceName) {
    }
}
