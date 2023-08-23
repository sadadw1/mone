package run.mone.hera.webhook.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.ObjectFieldSelector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import run.mone.hera.webhook.domain.Container;
import run.mone.hera.webhook.domain.JsonPatch;
import run.mone.hera.webhook.domain.Limits;
import run.mone.hera.webhook.domain.Resource;
import run.mone.hera.webhook.domain.VolumeMount;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/10 11:27 AM
 */
@Service
@Slf4j
public class HeraWebhookService {

    private static final String HOST_IP = "host.ip";
    private static final String NODE_IP = "node.ip";
    private static final String MIONE_PROJECT_ENV_NAME = "MIONE_PROJECT_ENV_NAME";
    private static final String MIONE_PROJECT_NAME = "MIONE_PROJECT_NAME";
    // cad need env
    // "-" replace of "_"
    private static final String APPLICATION = "APPLICATION";
    private static final String SERVER_ENV = "serverEnv";
    private static final String LOG_AGENT_CONTAINER_NAME = "log-agent";

    /**
     * log-agent默认镜像
     */
    private static final String LOG_AGENT_IMAGE = "herahub/opensource-pub:log-agent-v1-release";
    private static final String LOG_AGENT_NACOS_ADDR = "nacos.hera-namespace:80";
    private static final String LOG_AGENT_NACOS_ENV_KEY = "nacosAddr";
    private static final String LOG_AGENT_RESOURCE_CPU = "300m";
    private static final String LOG_AGENT_RESOURCE_MEMORY = "1Gi";

    /**
     * 针对zheli集群定制，log-agent volumeMounts subPathExpr 按照这个顺序取值拼接
     */
    private static final List<String> ZHELI_LOG_AGENT_ENV = Arrays.asList("K8S_NAMESPACE", "K8S_SERVICE", "POD_NAME", "K8S_LANGUAGE");

    /**
     * 添加log-agent条件：
     * 1、pod name prefix必须在 ${LOG_AGENT_POD_CONDITION_POD_PREFIX} 之中
     * 2、pod必须至少有一个容器
     * 3、pod中第一个容器内，环境变量必须包含 ${LOG_AGENT_CONDITION_ENV} 中定义的环境变量
     * 4、pod中第一个容器内，必须要有volumeMounts，并且volumeMounts中必须至少有一个name在 ${LOG_AGENT_VOLUME_MOUNTS_NAME} 中
     * 5、pod所在namespace属于 ${LOG_AGENT_CONDITION_NAMESPACE} 之中
     * 5、只处理类型为POD，且operation为CREATE的请求
     */

    private final List<String> podPrefixes = new ArrayList<>();
    private final List<String> logAgentConditionEnvs = new ArrayList<>();
    private final List<String> logAgentConditionVolumeMountNames = new ArrayList<>();
    private final List<String> logAgentConditionNameSpaces = new ArrayList<>();

    @PostConstruct
    private void init() {
        String logAgentConditionPodPrefix = System.getenv("LOG_AGENT_POD_CONDITION_POD_PREFIX");
        if (StringUtils.isNotEmpty(logAgentConditionPodPrefix)) {
            podPrefixes.addAll(Arrays.asList(logAgentConditionPodPrefix.split(",")));
        }
//        String logAgentConditionEnv = System.getenv("LOG_AGENT_CONDITION_ENV");
//        if (StringUtils.isNotEmpty(logAgentConditionEnv)) {
//            logAgentConditionEnvs.addAll(Arrays.asList(logAgentConditionEnv.split(",")));
//        }
        logAgentConditionEnvs.addAll(ZHELI_LOG_AGENT_ENV);
        String logAgentConditionVolumeMountsName = System.getenv("LOG_AGENT_VOLUME_MOUNTS_NAME");
        if (StringUtils.isNotEmpty(logAgentConditionVolumeMountsName)) {
            logAgentConditionVolumeMountNames.addAll(Arrays.asList(logAgentConditionVolumeMountsName.split(",")));
        }
        String logAgentConditionNameSpace = System.getenv("LOG_AGENT_CONDITION_NAMESPACE");
        if (StringUtils.isNotEmpty(logAgentConditionNameSpace)) {
            logAgentConditionNameSpaces.addAll(Arrays.asList(logAgentConditionNameSpace.split(",")));
        }
    }

    public List<JsonPatch> setPodEnv(JSONObject admissionRequest) {
        if (!includeNameSpace(admissionRequest.getString("namespace"))) {
            log.warn("setPodEnv name space is invalid");
            return null;
        }
        if (!filterByPodName(admissionRequest)) {
            log.warn("setPodEnv pod name prefix is invalid");
            return null;
        }
        String operation = admissionRequest.getString("operation");
        if (!"CREATE".equals(operation) && !"UPDATE".equals(operation)) {
            log.warn("setPodEnv operator is invalid");
            return null;
        }
        List<JsonPatch> result = new ArrayList<>();
        JSONArray containersJson = admissionRequest.getJSONObject("object").getJSONObject("spec").getJSONArray("containers");
        for (int i = 0; i < containersJson.size(); i++) {
            JSONObject container = containersJson.getJSONObject(i);
            if (container != null) {
                processContainerEnv(container, i, result);
            }
        }
        return result;
    }

    private void processContainerEnv(JSONObject container, int index, List<JsonPatch> result) {
        JSONArray env = container.getJSONArray("env");
        String basePath = "/spec/containers/" + index + "/env";
        // don't have env element
        if (env == null) {
            List<EnvVar> envs = new ArrayList<>();
            envs.add(createPodIdEnv());
            envs.add(createNodeIdEnv());
            result.add(new JsonPatch("add", basePath, envs));
        } else {
            Set<String> envKeys = envKeys(env);
            String path = basePath + "/-";
            addIfAbsent(result, path, HOST_IP, createPodIdEnv(), envKeys);
            addIfAbsent(result, path, NODE_IP, createNodeIdEnv(), envKeys);
            //set cadvisor need env
            setCadvisorEnv(result, path, env, envKeys);
        }
    }

    private void setCadvisorEnv(List<JsonPatch> result, String path, JSONArray env, Set<String> envKeys) {
        for (int j = 0; j < env.size(); j++) {
            JSONObject envJson = env.getJSONObject(j);
            String envKey = envJson.getString("name");
            String envValue = envJson.getString("value");

            if (!envKeys.contains(APPLICATION) && MIONE_PROJECT_NAME.equals(envKey)) {
                result.add(new JsonPatch("add", path, buildEnv(APPLICATION, envValue.replaceAll("-", "_"))));
            }
            if (!envKeys.contains(SERVER_ENV) && MIONE_PROJECT_ENV_NAME.equals(envKey)) {
                result.add(new JsonPatch("add", path, buildEnv(SERVER_ENV, envValue)));
            }
        }
    }

    private void addIfAbsent(List<JsonPatch> result, String path, String key, EnvVar envVar, Set<String> envKeys) {
        if (!envKeys.contains(key)) {
            result.add(new JsonPatch("add", path, envVar));
        }
    }

    public void setLogAgent(JSONObject admissionRequest, List<JsonPatch> jsonPatches) {
        // 按要求过滤
        if (!includeNameSpace(admissionRequest.getString("namespace"))) {
            log.warn("setLogAgent name space is invalid");
            return;
        }
        if (!filterByPodName(admissionRequest)) {
            log.warn("setLogAgent pod name prefix is invalid");
            return;
        }
        JSONArray containersJson = admissionRequest.getJSONObject("object").getJSONObject("spec").getJSONArray("containers");
        if (containersJson == null || containersJson.size() == 0) {
            log.warn("setLogAgent container is null");
            return;
        }
        JSONObject container = containersJson.getJSONObject(0);
        if (!includeEnv(container)) {
            log.warn("setLogAgent env is invalid");
            return;
        }
        if (!includeVolumeMounts(container)) {
            log.warn("setLogAgent volume mounts is invalid");
            return;
        }
        String operation = admissionRequest.getString("operation");
        if (!"CREATE".equals(operation)) {
            log.warn("setLogAgent operation is invalid");
            return;
        }

        // 判断是否存在log-agent，以及获取主容器挂载出来的日志路径信息与Env信息
        if (LOG_AGENT_CONTAINER_NAME.equals(container.getString("name"))) {
            log.warn("setLogAgent log-agent container is exist");
            return;
        }

        // 获取业务应用的volumeMounts，需要将log-agent的volumeMounts设置为与主容器相同的目录
        List<EnvVar> envs = new ArrayList<>();
        VolumeMount volumeMount = getVolumeMountAndAddEnvOnZheli(container, envs, admissionRequest.getString("name"));
        if (volumeMount == null) {
            log.warn("setLogAgent volume mounts is null");
        }
        // 如果满足条件， 则新建log-agent容器
        buildLogAgentContainer(volumeMount, envs, jsonPatches);
    }

    private VolumeMount getVolumeMountAndAddEnv(JSONObject container, List<EnvVar> envs) {
        JSONArray volumeMountsJson = container.getJSONArray("volumeMounts");
        log.info("volumeMountsJson is : " + volumeMountsJson);
        if (volumeMountsJson != null) {
            for (int i = 0; i < volumeMountsJson.size(); i++) {
                JSONObject volumeMountJson = volumeMountsJson.getJSONObject(i);
                String volumeMountName = volumeMountJson.getString("name");
                log.info("volumeMountName is : " + volumeMountName);
                if (logAgentConditionVolumeMountNames.contains(volumeMountName)) {
                    // 复制env
                    JSONArray env = container.getJSONArray("env");
                    envs.addAll(copyEnvForLogAgent(env));
                    return copyVolumeForLogAgent(volumeMountJson);
                }
            }
        }
        return null;
    }

    private VolumeMount getVolumeMountAndAddEnvOnZheli(JSONObject container, List<EnvVar> envs, String podName) {
        JSONArray volumeMountsJson = container.getJSONArray("volumeMounts");
        log.info("volumeMountsJson is : " + volumeMountsJson);
        if (volumeMountsJson != null) {
            for (int i = 0; i < volumeMountsJson.size(); i++) {
                JSONObject volumeMountJson = volumeMountsJson.getJSONObject(i);
                String volumeMountName = volumeMountJson.getString("name");
                log.info("volumeMountName is : " + volumeMountName);
                if (logAgentConditionVolumeMountNames.contains(volumeMountName)) {
                    // 复制env
                    JSONArray env = container.getJSONArray("env");
                    envs.addAll(copyEnvForLogAgent(env));
                    return copyVolumeForLogAgentOnZheli(volumeMountJson, envs, podName);
                }
            }
        }
        return null;
    }

    private void buildLogAgentContainer(VolumeMount volumeMount, List<EnvVar> envs, List<JsonPatch> jsonPatches) {
        if (jsonPatches == null) {
            jsonPatches = new ArrayList<>();
        }
        String path = "/spec/containers/-";
        Container container = new Container();
        container.setName(LOG_AGENT_CONTAINER_NAME);
        container.setImage(LOG_AGENT_IMAGE);

        Limits limits = new Limits();
        limits.setCpu(LOG_AGENT_RESOURCE_CPU);
        limits.setMemory(LOG_AGENT_RESOURCE_MEMORY);
        Resource resource = new Resource();
        resource.setLimits(limits);
        container.setResources(resource);

        container.setVolumeMounts(Collections.singletonList(volumeMount));

        envs.add(buildEnv(LOG_AGENT_NACOS_ENV_KEY, LOG_AGENT_NACOS_ADDR));
        container.setEnv(envs);
        log.info("log agent Container is : " + container);
        jsonPatches.add(new JsonPatch<>("add", path, container));
        log.info("log agent added");
    }

    private VolumeMount copyVolumeForLogAgent(JSONObject volumeMountJson) {
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setName(volumeMountJson.getString("name"));
        volumeMount.setMountPath(volumeMountJson.getString("mountPath"));
        volumeMount.setSubPathExpr(volumeMountJson.getString("subPathExpr"));
        return volumeMount;
    }

    private VolumeMount copyVolumeForLogAgentOnZheli(JSONObject volumeMountJson, List<EnvVar> envs, String podName) {
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setName(volumeMountJson.getString("name"));
        volumeMount.setMountPath(volumeMountJson.getString("mountPath"));
//        volumeMount.setSubPathExpr(volumeMountJson.getString("subPathExpr"));

        // zheli定制，将subPathExpr按照logAgentConditionEnvs顺序取值拼接，除了POD_NAME，不能出现引用，不能出现空值
        String k8sNamespace = getEnvValue("K8S_NAMESPACE", envs);
        String k8sService = getEnvValue("K8S_SERVICE", envs);
        String k8sLanguage = getEnvValue("K8S_LANGUAGE", envs);
        if(StringUtils.isEmpty(k8sNamespace) || StringUtils.isEmpty(k8sService) || StringUtils.isEmpty(k8sLanguage)){
            log.warn("setLogAgent env k8sNamespace or k8sService or k8sLanguage is empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(k8sNamespace).append("/").append(k8sService).append("/").append("${POD_NAME}").append("/").append(k8sLanguage);
        volumeMount.setSubPathExpr(sb.toString());
        return volumeMount;
    }

    private String getEnvValue(String envKey, List<EnvVar> envs) {
        for (EnvVar envVar : envs) {
            if (envKey.equals(envVar.getName())) {
                return envVar.getValue();
            }
        }
        return null;
    }

    private boolean includeEnv(JSONObject container) {
        if (logAgentConditionEnvs.size() == 0) {
            return false;
        }
        JSONArray env = container.getJSONArray("env");
        if (env == null || env.size() == 0) {
            return false;
        }
        Set<String> envNames = new HashSet<>();
        for (int j = 0; j < env.size(); j++) {
            envNames.add(env.getJSONObject(j).getString("name"));
        }
        return envNames.containsAll(logAgentConditionEnvs);
    }

    private boolean includeVolumeMounts(JSONObject container) {
        if (logAgentConditionVolumeMountNames.size() == 0) {
            return false;
        }
        JSONArray volumeMountsJson = container.getJSONArray("volumeMounts");
        if (volumeMountsJson != null) {
            for (int j = 0; j < volumeMountsJson.size(); j++) {
                JSONObject volumeMountJson = volumeMountsJson.getJSONObject(j);
                if (logAgentConditionVolumeMountNames.contains(volumeMountJson.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<EnvVar> copyEnvForLogAgent(JSONArray envsJson) {
        List<EnvVar> envs = new ArrayList<>();
        if (envsJson != null && envsJson.size() > 0) {
            for (int i = 0; i < envsJson.size(); i++) {
                JSONObject envJson = envsJson.getJSONObject(i);
                String name = envJson.getString("name");
                if (logAgentConditionEnvs.contains(name)) {
                    EnvVar env = JSON.toJavaObject(envJson, EnvVar.class);
                    envs.add(env);
                }
            }
        }
        return envs;
    }

    private EnvVar createPodIdEnv() {
        return buildEnvRef(HOST_IP, "v1", "status.podIP");
    }

    private EnvVar createNodeIdEnv() {
        return buildEnvRef(NODE_IP, "v1", "status.hostIP");
    }

    private EnvVar buildEnv(String key, String value) {
        EnvVar env = new EnvVar();
        env.setName(key);
        env.setValue(value);
        return env;
    }

    private EnvVar buildEnvRef(String key, String apiVersion, String fieldPath) {
        EnvVar env = new EnvVar();
        env.setName(key);
        EnvVarSource envVarSource = new EnvVarSource();
        ObjectFieldSelector objectFieldSelector = new ObjectFieldSelector();
        objectFieldSelector.setApiVersion(apiVersion);
        objectFieldSelector.setFieldPath(fieldPath);
        envVarSource.setFieldRef(objectFieldSelector);
        env.setValueFrom(envVarSource);
        return env;
    }

    private Set<String> envKeys(JSONArray envs) {
        Set<String> keySet = new HashSet<>();
        if (envs != null && envs.size() > 0) {
            for (int i = 0; i < envs.size(); i++) {
                keySet.add(envs.getJSONObject(i).getString("name"));
            }
        }
        return keySet;
    }

    private boolean filterByPodName(JSONObject admissionRequest) {
        if (podPrefixes.size() == 0) {
            return false;
        }
        // 按podName前缀过滤
        String name = admissionRequest.getString("name");
        if (StringUtils.isEmpty(name)) {
            JSONObject metadata = admissionRequest.getJSONObject("object").getJSONObject("metadata");
            name = metadata.getString("generateName");
        }
        log.info("setLogAgent get pod name is : " + name);
        for (String prefix : podPrefixes) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean includeNameSpace(String namespace) {
        if (StringUtils.isEmpty(namespace) || logAgentConditionNameSpaces.size() == 0) {
            return false;
        }
        return logAgentConditionNameSpaces.contains(namespace);
    }
}
