package com.glriverside.gaop.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class LockManagerTest {
    @Test
    void 服务器开机后_服务需要分批启动() throws Exception {
        LockManager lockManager = new LockManager();
        ArrayList<List<String>> batch = new ArrayList<>();
        batch.add(Arrays.asList("gaop-web", "b1", "c1", "d1"));
        batch.add(Arrays.asList("a2", "b2", "c2", "d2"));
        batch.add(Arrays.asList("a3", "b3", "c3", "d3"));
        lockManager.setBatchOrder(batch);
//        assertTrue(lockManager.isBootable("c1"));
//        assertFalse(lockManager.isBootable("c2"));
//        assertTrue(lockManager.isBootable("b1"));
//        assertFalse(lockManager.isBootable("b2"));
//        assertTrue(lockManager.isBootable("a1"));
//        assertFalse(lockManager.isBootable("c3"));
//        assertTrue(lockManager.isBootable("d1"));
//        assertFalse(lockManager.isBootable("b3"));
//        assertTrue(lockManager.isBootable("a2"));
//        assertFalse(lockManager.isBootable("a3"));
//        assertTrue(lockManager.isBootable("b2"));
//        assertFalse(lockManager.isBootable("c3"));
//        assertTrue(lockManager.isBootable("c2"));
//        assertTrue(lockManager.isBootable("d2"));
//        assertTrue(lockManager.isBootable("c3"));
//        assertTrue(lockManager.isBootable("b1"));
//        assertFalse(lockManager.isBootable("d3"));
//        assertTrue(lockManager.isBootable("b1"));
//        assertTrue(lockManager.isBootable("b3"));

        lockManager.printMap();
    }

    @Test
    void 服务器正常启动后_手动重启某服务不能延迟启动() throws Exception {
        LockManager lockManager = new LockManager();
        ArrayList<List<String>> batch = new ArrayList<>();
        batch.add(Arrays.asList("a1", "b1", "c1", "d1"));
        batch.add(Arrays.asList("a2", "b2", "c2", "d2"));
        batch.add(Arrays.asList("a3", "b3", "c3", "d3"));
        lockManager.setBatchOrder(batch);
        assertTrue(lockManager.isBootable("a1"));
        assertFalse(lockManager.isBootable("c2"));
        assertTrue(lockManager.isBootable("c1"));//c1第一次允许启动
        assertTrue(lockManager.isBootable("b1"));
        assertTrue(lockManager.isBootable("c1"));//c1所在批次还没结束，看是否允许启动c1（手动重启）
        assertTrue(lockManager.isBootable("d1"));
        assertTrue(lockManager.isBootable("a2"));
        assertTrue(lockManager.isBootable("c1"));//c1所在批次都允许启动了,看是否允许启动c1（手动重启）
        assertTrue(lockManager.isBootable("a2"));//c1所在批次都允许启动了,看是否允许启动c1（手动重启）
    }

    @Test
    void Lock服务重启_其他服务正常运行_手动重启服务不能延迟启动() throws Exception {
        LockManager lockManager = new LockManager();
        ArrayList<List<String>> batch = new ArrayList<>();
        batch.add(Arrays.asList("a1", "b1", "c1", "d1"));
        batch.add(Arrays.asList("a2", "b2", "c2", "d2"));
        batch.add(Arrays.asList("a3", "b3", "c3", "d3"));
        lockManager.setBatchOrder(batch);
        assertTrue(lockManager.isBootable("a1"));
        assertTrue(lockManager.isBootable("b1"));
        assertTrue(lockManager.isBootable("c1"));
        assertTrue(lockManager.isBootable("d1"));
        //lock服务重启，但是这个是没有记录重启之前的批次和启动的状态，所以传入任何状态都可以正常启动
        LockManager lockManager1 = new LockManager();
        assertTrue(lockManager.isBootable("a1"));
        assertTrue(lockManager1.isBootable("b2"));

    }

}