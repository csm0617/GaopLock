package com.glriverside.gaop.lock;

import com.glriverside.gaop.lock.config.OrdersConfig;
import com.glriverside.gaop.lock.service.LockManagerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GaopLockApplicationTests {
    @Autowired
    private OrdersConfig ordersConfig;
    @Autowired
    private LockManagerService lockManagerService;
    @Test
    void contextLoads() throws Exception {
        String order = ordersConfig.getOrder();
        List<List<String>> batch = lockManagerService.stringToOrders(order);
        LockManager lockManager = new LockManager();
        lockManager.setBatchOrder(batch);
        lockManager.printMap();
    }

}
