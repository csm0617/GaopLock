package com.glriverside.gaop.lock.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class LockManagerService {
    public List<List<String>> stringToOrders(String order){
        List<List<String>> batchService = new ArrayList<>();
        String[]  batchOrder= order.split("\\|");
        for (String  batch: batchOrder) {
            String[] serviceNames = batch.split(",");
            List<String> services = new ArrayList<>(Arrays.asList(serviceNames));
            batchService.add(services);
        }
        return batchService;
    }
}
