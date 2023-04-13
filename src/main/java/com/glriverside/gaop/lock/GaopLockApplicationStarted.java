package com.glriverside.gaop.lock;

import com.glriverside.gaop.lock.config.K8sConfig;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.net.URL;

@Component
@Slf4j
public class GaopLockApplicationStarted implements ApplicationListener<ApplicationReadyEvent>{
    @Autowired
    private K8sConfig k8sConfig;
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        initK8sClient();
    }

    /**
     * 初始化k8s客户端配置
     */
    @SneakyThrows
    void initK8sClient() {
        if (!k8sConfig.isEnabled()) {
            return;
        }
        ApiClient client;
        if (k8sConfig.isInCluster()) {
            client = ClientBuilder.cluster()

                    .build();
        } else {
            final String conf = k8sConfig.getConfigFile();
            URL resource = GaopLockApplication.class.getClassLoader().getResource(conf);
            if (resource == null) {
                log.error("k8s config file 为 null: {}", conf);
                return;
            }
            final InputStreamReader input = new InputStreamReader(resource.openStream());
            client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(input)).build();
        }

        Configuration.setDefaultApiClient(client);

        if (k8sConfig.isLogEnabled()) {
            final ApiClient defaultApiClient = Configuration.getDefaultApiClient();
            final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(k8sConfig.getLogLevel()));
            final OkHttpClient build = defaultApiClient.getHttpClient().newBuilder()
                    .addInterceptor(httpLoggingInterceptor)
                    .build();
            defaultApiClient.setHttpClient(build);
            log.debug("k8s client: {}", defaultApiClient.getBasePath());
        }

        list();
//        updateInit();
    }


    @SneakyThrows
    public static void list() {
        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listPodForAllNamespaces(null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , null
                , false);
        for (V1Pod item : list.getItems()) {
            log.debug("po: {}, namespace: {}", item.getMetadata().getName(), item.getMetadata().getNamespace());
        }
    }

}
