package com.glriverside.gaop.lock;

import com.glriverside.gaop.lock.constant.K8s;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class LockManager {

    /**
     * 服务名：第几批
     */
    private Map<String, Integer> orders = new HashMap<>();
    /**
     * 服务名：查询时间列表
     */
    private Map<String, List<Long>> count = new HashMap<>();
    private List<String> batch1 = new ArrayList<>();
    private List<String> batch2 = new ArrayList<>();

    /**
     * 服务是否允许启动
     *
     * @return
     */
    public boolean isBootable(String service) throws Exception {
        // 先判断传入的服务名是否有效，无效则不管他是不是真实存在统一返回ture,服务名有效就判断他的批次
        boolean containsService = orders.containsKey(service);
        //如果服务在设置的批次的集合中
        if (containsService) {
            //记录服务请求得时间和重启的次数
            if (count.containsKey(service)) {
                List<Long> longs = count.get(service);
                longs.add(System.currentTimeMillis());
            } else {
                List<Long> value = new ArrayList<>();
                value.add(System.currentTimeMillis());
                count.put(service, value);
            }
            if (count.get(service).size() >= 11) {
                log.info("服务已经重启 " + (count.get(service).size() - 1) + " 次，超过了预设的10次，运行启动");
                orders.remove(service);
            }

            //根据service遍历order中的所有键值对
            for (String key : orders.keySet()) {
                if (key.equals(service)) {
                    //判断是不是在第一批里
                    if (orders.get(key) == 0) {
                        batch1.add(key);
                        int currentBatch = orders.get(key);
//                        log.info(key);
                        //如果是在第一批，那么在返回ture之前先把这个service从（服务名：批次）的map移除
                        log.info("第 " + (currentBatch + 1) + " 批的 " + key + " 允许启动");
                        orders.remove(key);
                        List<String> bathRestService = getBathRestService(orders, currentBatch);
                        //   (orders.get(key) key 已经remove了导致NPE
                        //   log.info("当前批次 " + (orders.get(key) + 1) + " 还有 " + Arrays.toString(bathRestService.toArray()) + " 没有启动");
                        if (bathRestService.size() != 0) {
                            log.info("当前批次 " + (currentBatch + 1) + " 还有 " + Arrays.toString(bathRestService.toArray()) + " 没有允许启动");
                        } else {
                            log.info("当前批次 " + (currentBatch + 1) + " 都已允许启动");
                        }
                        return true;
                    } else if (orders.get(key).equals(1)) {
                        batch2.add(key);
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
                            Map<String, Boolean> batch1PodStatus = getBatchPodStatus(batch1);
                            List<String> failedPodSet = getFailedPodSet(batch1PodStatus);
                            for (String pod : failedPodSet) {
                                orders.put(pod, currentBatch + 1);
                            }
                            log.info("第 " + currentBatch + " 批都已允许启动，" + "当前" + (currentBatch + 1) + " 批次的 " + key + " 允许启动");
                            //把service从第二批的map里移除
                            orders.remove(key);
                            List<String> bathRestService = getBathRestService(orders, currentBatch);
                            if (bathRestService.size() != 0) {
                                log.info("当前批次" + (currentBatch + 1) + " 还有 " + Arrays.toString(bathRestService.toArray()) + " 没有允许启动");
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
                            Map<String, Boolean> batch2PodStatus = getBatchPodStatus(batch2);
                            List<String> failedPodSet = getFailedPodSet(batch2PodStatus);
                            for (String pod : failedPodSet) {
                                orders.put(pod, currentBatch + 1);
                            }
                            log.info("第 " + currentBatch + " 批都已允许启动，" + "当前 " + (currentBatch + 1) + " 批次的 " + key + " 允许启动");
                            //把service从第二批的map里移除
                            orders.remove(key);
                            List<String> bathRestService = getBathRestService(orders, currentBatch);
                            if (bathRestService.size() != 0) {
                                log.info("当前批次" + (currentBatch + 1) + "还有 " + Arrays.toString(bathRestService.toArray()) + " 没有允许启动");
                            } else {
                                log.info("当前批次 " + (currentBatch + 1) + " 都已允许启动");
                            }
                            return true;
                        }
                    } else {
                        log.info(key + " 服务已经在重启过程超过了所在的批次允许启动");
                        orders.remove(key);
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
    public Map<String, Integer> setBatchOrder(List<List<String>> batch) throws Exception {
        Map<String, Boolean> allPodStatus = getAllPodStatus();
        //注意批次的下标从0开始
        for (int i = 0; i < batch.size(); i++) {
            List<String> services = batch.get(i);
            int finalI = i;
            services.forEach(service -> {
                Boolean success = allPodStatus.get(service);
                if (success != null) {
                    if (!success) {
                        orders.put(service, finalI);
                    }
                }
            });
        }
        return orders;
    }


    private Map<String, Boolean> getAllPodStatus() throws ApiException {
        Map<String, Boolean> podStatusMap = new HashMap<>();
        CoreV1Api coreV1Api = new CoreV1Api();
        V1PodList v1PodList = coreV1Api.listNamespacedPod(
                K8s.NAMESPACE,
                null,
                null,
                null,
                null,
//                "name="+serviceName,
                null,
                null,
                null,
                null,
                null,
                null);
        List<V1Pod> v1Pods = v1PodList.getItems();
//        List<Boolean> statusList = new ArrayList<>();
        for (V1Pod v1Pod : v1Pods) {
            String name = v1Pod.getMetadata().getLabels().get("name");
            List<V1ContainerStatus> containerStatuses = v1Pod.getStatus().getContainerStatuses();
            for (V1ContainerStatus containerStatus : containerStatuses) {
                Boolean status = containerStatus.getReady();
                podStatusMap.put(name, status);
            }
        }
        return podStatusMap;
    }

    private Map<String, Boolean> getBatchPodStatus(List<String> batch) throws ApiException {
        Map<String, Boolean> podStatusMap = new HashMap<>();
        CoreV1Api coreV1Api = new CoreV1Api();
        for (String serviceName : batch) {
            V1PodList v1PodList = coreV1Api.listNamespacedPod(
                    K8s.NAMESPACE,
                    null,
                    null,
                    null,
                    null,
                    "name=" + serviceName,
                    null,
                    null,
                    null,
                    null,
                    null);
            List<V1Pod> v1Pods = v1PodList.getItems();
//        List<Boolean> statusList = new ArrayList<>();
            for (V1Pod v1Pod : v1Pods) {
                String name = v1Pod.getMetadata().getLabels().get("name");
                List<V1ContainerStatus> containerStatuses = v1Pod.getStatus().getContainerStatuses();
                for (V1ContainerStatus containerStatus : containerStatuses) {
                    Boolean status = containerStatus.getReady();
                    podStatusMap.put(name, status);
                }
            }
        }
        return podStatusMap;
    }

    private List<String> getFailedPodSet(Map<String, Boolean> podStatus) {
        ArrayList<String> failed = new ArrayList<>();
        for (Map.Entry<String, Boolean> pod : podStatus.entrySet()) {
            if (!pod.getValue()) {
                failed.add(pod.getKey());
            }
        }
        return failed;
    }

//    public static Map<String, String> setBatch1Status(Map<String, String> map) {
//        map.put("a1", "ture");
//        map.put("b1", "false");
//        map.put("c1", "ture");
//        map.put("d1", "ture");
//        return map;
//    }

//    public static Map<String, String> setBatch2Status(Map<String, String> map) {
//        map.put("a2", "ture");
//        map.put("b2", "false");
//        map.put("c2", "false");
//        map.put("d2", "ture");
//        return map;
//    }
}
