package com.glriverside.gaop.lock.service;

import com.glriverside.gaop.lock.constant.K8s;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class GaopLockService {
    private Map<String,Boolean> getAllPodStatus() throws ApiException {
        Map<String,Boolean> podStatusMap = new HashMap<>();
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
                podStatusMap.put(name,status);
            }
        }
        return podStatusMap;
    }
}
