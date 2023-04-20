package com.glriverside.gaop.lock.controller;

import com.glriverside.gaop.lock.LockManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/lock")
@RestController
public class LockController {

    private LockManager lockManager = new LockManager();
    @GetMapping("isBootable/{serviceName}")
    public int check(@PathVariable String serviceName) throws Exception {
        boolean bootable = lockManager.isBootable(serviceName);
        if (bootable){
            return 9527;
        }else {
            return 9528;
        }
    }
}
