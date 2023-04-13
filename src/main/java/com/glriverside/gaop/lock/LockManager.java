package com.glriverside.gaop.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class LockManager {
    /**
     * 服务名：第几批
     */
    private Map<String, Integer> orders = new HashMap<>();

    private Map<String, String> status1 = new HashMap<>();

    private Map<String, String> status2 = new HashMap<>();
    /**
     * 服务名：查询时间列表
     */
    private Map<String, List<Long>> count = new HashMap<>();

    /**
     * 服务是否允许启动
     *
     * @return
     */
    public boolean isBootable(String service) {
        Map<String, String> batch1Status = setBatch1Status(status1);
        Map<String, String> batch2Status = setBatch2Status(status2);

        // 先判断传入的服务名是否有效，无效则不管他是不是真实存在统一返回ture,服务名有效就判断他的批次
        boolean containsService = orders.containsKey(service);
        //如果服务在设置的批次的集合中
        if (containsService) {
            //根据service遍历order中的所有键值对
            for (String key : orders.keySet()) {
                if (key.equals(service)) {
                    //判断是不是在第一批里
                    if (orders.get(key) == 0) {
                        int currentBatch = orders.get(key);
//                        log.info(key);
                        //如果是在第一批，那么在返回ture之前先把这个service从（服务名：批次）的map移除
                        log.info("第 " + (currentBatch + 1) + " 批的 " + key + " 允许启动");
                        orders.remove(key);
                        List<String> bathRestService = getBathRestService(orders, currentBatch);
                        //   (orders.get(key) key 已经remove了导致NPE
                        //   log.info("当前批次 " + (orders.get(key) + 1) + " 还有 " + Arrays.toString(bathRestService.toArray()) + " 没有启动");
                        if (bathRestService.size()!=0) {
                            log.info("当前批次 " + (currentBatch + 1) + " 还有 " + Arrays.toString(bathRestService.toArray()) + " 没有允许启动");
                        }else {
                            log.info("当前批次 " + (currentBatch + 1) + " 都已允许启动");
                        }
                        return true;
                    } else if (orders.get(key).equals(1)) {
                        int currentBatch = orders.get(key);
                        int beforeBath = currentBatch - 1;
                        //首先判断第一批的程序是不是都已经允许启动了
                        //1.先获取orders中value为1的key的size的大小
                        //2.size为0说明第一批的已经全部允许启动了，至于是不是都已经成功运行了我们先不管
                        Integer rest = getRestSize(orders, beforeBath);
                        if (rest > 0) {
                            List<String> bathRestService = getBathRestService(orders, beforeBath);
                            //说明第一批还没有允许全部启动，第二批的也不允许启动
                            log.info("第 " + (beforeBath + 1) + " 批的 " + Arrays.toString(bathRestService.toArray()) + " 还没有允许启动，" + "第 " + (currentBatch + 1) + " 批的 " + key + " 已经发送请求，不允许启动");
                            return false;
                        } else if (rest == 0) {
                            //说明第一批启动完了
                            //把k8s的api查出来的(服务名：状态)
                            //把查出来的状态为fasle且为第一批放到第二批中
                            for (Map.Entry<String, String> statusMap : batch1Status.entrySet()) {
                                if (statusMap.getValue().equals("false")) {
                                    //把启动失败的的放到下一批
                                    orders.put(statusMap.getKey(), currentBatch + 1);
                                }
                            }
                            log.info("第 " + currentBatch + " 批都已允许启动，" + "当前" + (currentBatch + 1) + " 批次的 " + key + " 允许启动");
                            //把service从第二批的map里移除
                            orders.remove(key);
                            List<String> bathRestService = getBathRestService(orders, currentBatch);
                            if (bathRestService.size() != 0) {
                                log.info("当前批次"+(currentBatch+1)+"还有 " + Arrays.toString(bathRestService.toArray()) + " 没有允许启动");
                            } else {
                                log.info("当前批次 " + (currentBatch + 1) + " 都已允许启动");
                            }
                            return true;
                        }
                    } else if (orders.get(key).equals(2)) {
                        int currentBatch = orders.get(key);
                        int beforeBath = currentBatch - 1;
                        Integer rest = getRestSize(orders, beforeBath);
                        if (rest > 0) {
                            List<String> bathRestService = getBathRestService(orders, beforeBath);
                            log.info("第 " + currentBatch + " 批的 " + Arrays.toString(bathRestService.toArray()) + " 还没有允许启动，" + "第 " + (currentBatch + 1) + " 批的 " + key + " 已经发送请求，不允许启动");
                            return false;
                        } else if (rest == 0) {
                            //说明第一批启动完了
                            //把k8s的api查出来的(服务名：状态)
                            //把查出来的状态为fasle且为第一批放到第二批中
                            for (Map.Entry<String, String> statusMap : batch2Status.entrySet()) {
                                if (statusMap.getValue().equals("false")) {
                                    orders.put(statusMap.getKey(), currentBatch + 1);
                                }
                            }
                            log.info("第 " + currentBatch + " 批都已允许启动，" + "当前 " + (currentBatch + 1) + " 批次的 " + key + " 允许启动");
                            ;
                            //把service从第二批的map里移除
                            orders.remove(key);
                            List<String> bathRestService = getBathRestService(orders, currentBatch);
                            if (bathRestService.size() != 0) {
                                log.info("当前批次"+(currentBatch+1)+"还有 " + Arrays.toString(bathRestService.toArray()) + " 没有允许启动");
                            } else {
                                log.info("当前批次 " + (currentBatch + 1) + " 都已允许启动");
                            }
                            return true;
                        }
                    }
                }
            }
        } else {
            //服务不在集合的批次中，或者服务名无效直接返回ture，就不需要等待
            log.info(service + "服务已经允许启动过了或不在设定的批次中，允许启动");
            return true;

        }

        return false;
    }

    public void printMap() {
        for (Map.Entry<String, Integer> order : orders.entrySet()) {
            log.info(order.getKey() + ":" + order.getValue());
        }
    }

    public Integer getRestSize(Map<String, Integer> orders, Integer batch) {
        Integer size = 0;
        for (Map.Entry<String, Integer> order : orders.entrySet()) {
            if (order.getValue().equals(batch)) {
                size++;
            }
        }
        return size;
    }

    public List<String> getBathRestService(Map<String, Integer> orders, Integer batch) {
        List<String> restService = new ArrayList<>();
        for (Map.Entry<String, Integer> restServiceEntry : orders.entrySet()) {
            if (restServiceEntry.getValue().equals(batch)) {
                restService.add(restServiceEntry.getKey());
            }
        }
        return restService;
    }

    /**
     * 设置服务的启动批次，
     * 传参：
     *
     * @param batch
     */
    public void setBatchOrder(List<List<String>> batch) {
        //注意批次的下标从0开始
        for (int i = 0; i < batch.size(); i++) {
            List<String> services = batch.get(i);
            int finalI = i;
            services.forEach(service -> {
                orders.put(service, finalI);
            });
        }
    }

    public static Map<String, String> setBatch1Status(Map<String, String> map) {
        map.put("a1", "ture");
        map.put("b1", "false");
        map.put("c1", "ture");
        map.put("d1", "ture");
        return map;
    }

    public static Map<String, String> setBatch2Status(Map<String, String> map) {
        map.put("a2", "ture");
        map.put("b2", "false");
        map.put("c2", "false");
        map.put("d2", "ture");
        return map;
    }
}
