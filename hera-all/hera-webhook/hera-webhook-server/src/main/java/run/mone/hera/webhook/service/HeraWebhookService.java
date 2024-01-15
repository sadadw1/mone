package run.mone.hera.webhook.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.ObjectFieldSelector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import run.mone.hera.webhook.common.HttpClientUtil;
import run.mone.hera.webhook.domain.Container;
import run.mone.hera.webhook.domain.JsonPatch;
import run.mone.hera.webhook.domain.Limits;
import run.mone.hera.webhook.domain.Requests;
import run.mone.hera.webhook.domain.Resource;
import run.mone.hera.webhook.domain.TpcAppInfo;
import run.mone.hera.webhook.domain.VolumeMount;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private static final String MIONE_PROJECT_ENV_ID = "MIONE_PROJECT_ENV_ID";
    private static final String MIONE_PROJECT_NAME = "MIONE_PROJECT_NAME";
    // cad need env
    // "-" replace of "_"
    private static final String APPLICATION = "application";
    private static final String SERVER_ENV = "serverEnv";
    private static final String LOG_AGENT_CONTAINER_NAME = "log-agent";
    private static final String POD_IP_CAD = "POD_IP";
    private static final String NODE_IP_CAD = "NODE_IP";

    /**
     * operator need labels key
     */
    private static final String KEY_OZHERA_APP_NAME = "OZHERA_APP_NAME";
    private static final String KEY_OZHERA_APP_ENV_ID = "OZHERA_APP_ENV_ID";
    private static final String KEY_OZHERA_APP_ENV_NAME = "OZHERA_APP_ENV_NAME";
    private static final String APP_LABEL_KEY = "app";

    /**
     * log-agent默认镜像
     */
    private static final String DEFAULT_LOG_AGENT_IMAGE = "zheli-docker-registry-registry-vpc.cn-beijing.cr.aliyuncs.com/sre/oz-log-agent:v1-release";
    private static final String LOG_AGENT_IMAGE_ENV_KEY = "LOG_AGENT_IMAGE";
    private static final String LOG_AGENT_NACOS_ADDR = "nacos.ozhera-namespace:80";
    private static final String LOG_AGENT_NACOS_ENV_KEY = "nacosAddr";
    private static final String LOG_AGENT_RESOURCE_CPU_REQUESTS = "300m";
    private static final String LOG_AGENT_RESOURCE_MEMORY_REQUESTS = "1Gi";
    private static final String LOG_AGENT_RESOURCE_CPU_LIMITS = "1";
    private static final String LOG_AGENT_RESOURCE_MEMORY_LIMITS = "2Gi";

    /**
     * 针对zheli集群定制，log-agent volumeMounts subPathExpr 按照这个顺序取值拼接
     */
    private static final String APP_PREFIX = "zheli";
    private static final String K8S_NAMESPACE = "K8S_NAMESPACE";
    private static final String K8S_SERVICE = "K8S_SERVICE";
    private static final String POD_NAME = "POD_NAME";
    private static final String K8S_LANGUAGE = "K8S_LANGUAGE";

    private static final String K8S_ENV = "K8S_ENV";
    private static final String K8S_APP_COUNTRY = "K8S_APP_COUNTRY";

    private static final List<String> ZHELI_LOG_AGENT_ENV = Arrays.asList(K8S_NAMESPACE, K8S_SERVICE, POD_NAME, K8S_LANGUAGE);

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

    /**
     * 缓存
     */
    private static final Cache<String, TpcAppInfo> CACHE =
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofMinutes(10)).build();
    private static final String TPC_URL = "http://mi-tpc:8097/backend/node/inner_list";
    private static final Map<String, String> TPC_HEADER = new HashMap<>();
    private static final String TPC_TOKEN = "Ldwi$238DidsafFLDS&)$@!";
    private static final String TPC_APP_REQUEST_BODY = "{\"type\": 4, \"status\": 0, \"token\":\"" + TPC_TOKEN + "\"}";

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

        TPC_HEADER.put("Content-Type", "application/json");
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
        if (!"CREATE".equals(operation)) {
            log.warn("setPodEnv operator is invalid");
            return null;
        }
        List<JsonPatch> result = new ArrayList<>();
        String appLabelValue = getLabel(APP_LABEL_KEY, admissionRequest.getJSONObject("object").getJSONObject("metadata").getJSONObject("labels"));
        JSONArray containersJson = admissionRequest.getJSONObject("object").getJSONObject("spec").getJSONArray("containers");
        for (int i = 0; i < containersJson.size(); i++) {
            JSONObject container = containersJson.getJSONObject(i);
            if (container != null) {
                processContainerEnv(container, i, result, appLabelValue);
            }
        }
        return result;
    }

    public void setPodLabels(JSONObject admissionRequest, List<JsonPatch> result) {
        if (!includeNameSpace(admissionRequest.getString("namespace"))) {
            log.warn("setPodLabels name space is invalid");
            return;
        }
        if (!filterByPodName(admissionRequest)) {
            log.warn("setPodLabels pod name prefix is invalid");
            return;
        }
        String operation = admissionRequest.getString("operation");
        if (!"CREATE".equals(operation)) {
            log.warn("setPodLabels operator is invalid");
            return;
        }
        if (result == null) {
            result = new ArrayList<>();
        }
        JSONObject metadataJson = admissionRequest.getJSONObject("object").getJSONObject("metadata");
        setOzHeraLabels(result, metadataJson);
    }

    private void processContainerEnv(JSONObject container, int index, List<JsonPatch> result, String appLabelValue) {
        JSONArray env = container.getJSONArray("env");
        String envBasePath = "/spec/containers/" + index + "/env";
        // don't have env element
        if (env == null) {
            List<EnvVar> envs = new ArrayList<>();
            envs.add(createPodIdEnv());
            envs.add(createNodeIdEnv());
            envs.add(createPodIpCADEnv());
            envs.add(createNodeIpCADEnv());
            result.add(new JsonPatch("add", envBasePath, envs));
        } else {
            Set<String> envKeys = envKeys(env);
            String path = envBasePath + "/-";
            addIfAbsent(result, path, HOST_IP, createPodIdEnv(), envKeys);
            addIfAbsent(result, path, NODE_IP, createNodeIdEnv(), envKeys);
            addIfAbsent(result, path, POD_IP_CAD, createPodIpCADEnv(), envKeys);
            addIfAbsent(result, path, NODE_IP_CAD, createNodeIpCADEnv(), envKeys);
            // get appInfo
            TpcAppInfo appInfo = getAppInfo(env, appLabelValue);
            if (appInfo != null) {
                //set ozhera env
                setOzHeraEnvs(result, path, appInfo, envKeys);
            }
        }
    }

    private void setOzHeraEnvs(List<JsonPatch> result, String path, TpcAppInfo appInfo, Set<String> envKeys) {
        if (!envKeys.contains(APPLICATION) && StringUtils.isNotEmpty(appInfo.getIdAndName())) {
            result.add(new JsonPatch("add", path, buildEnv(APPLICATION, appInfo.getIdAndName().replaceAll("-", "_"))));
        }
        if (!envKeys.contains(SERVER_ENV) && StringUtils.isNotEmpty(appInfo.getEnvName())) {
            result.add(new JsonPatch("add", path, buildEnv(SERVER_ENV, appInfo.getEnvName())));
        }
        if (!envKeys.contains(MIONE_PROJECT_NAME) && StringUtils.isNotEmpty(appInfo.getIdAndName())) {
            result.add(new JsonPatch("add", path, buildEnv(MIONE_PROJECT_NAME, appInfo.getIdAndName())));
        }
        if (!envKeys.contains(MIONE_PROJECT_ENV_ID) && appInfo.getEnvId() != null && appInfo.getEnvId() != 0) {
            result.add(new JsonPatch("add", path, buildEnv(MIONE_PROJECT_ENV_ID, String.valueOf(appInfo.getEnvId()))));
        }
        if (!envKeys.contains(MIONE_PROJECT_ENV_NAME) && StringUtils.isNotEmpty(appInfo.getEnvName())) {
            result.add(new JsonPatch("add", path, buildEnv(MIONE_PROJECT_ENV_NAME, appInfo.getEnvName())));
        }
    }

    private void setOzHeraLabels(List<JsonPatch> result, JSONObject metadataJson) {
        JSONObject labelsJson = metadataJson.getJSONObject("labels");
        String app = getLabel(APP_LABEL_KEY, labelsJson);
        if (StringUtils.isNotEmpty(app)) {
            TpcAppInfo tpcAppInfo = CACHE.asMap().get(app);
            if (tpcAppInfo != null) {
                String path = "/metadata/labels";
                if (!labelsJson.containsKey(KEY_OZHERA_APP_NAME) && StringUtils.isNotEmpty(tpcAppInfo.getName())) {
                    result.add(new JsonPatch("replace", path + "/" + KEY_OZHERA_APP_NAME, tpcAppInfo.getName()));
                }
                if (!labelsJson.containsKey(KEY_OZHERA_APP_ENV_ID) && tpcAppInfo.getEnvId() != null && tpcAppInfo.getEnvId() != 0) {
                    result.add(new JsonPatch("replace", path + "/" + KEY_OZHERA_APP_ENV_ID, String.valueOf(tpcAppInfo.getEnvId())));
                }
                if (!labelsJson.containsKey(KEY_OZHERA_APP_ENV_NAME) && StringUtils.isNotEmpty(tpcAppInfo.getEnvName())) {
                    result.add(new JsonPatch("replace", path + "/" + KEY_OZHERA_APP_ENV_NAME, tpcAppInfo.getEnvName()));
                }
            }
        }
    }

    private String getLabel(String key, JSONObject labelsJson) {
        if (StringUtils.isNotEmpty(key) && labelsJson != null && !labelsJson.isEmpty()) {
            return labelsJson.getString(key);
        }
        return null;
    }


    private void addIfAbsent(List<JsonPatch> result, String path, String key, EnvVar
            envVar, Set<String> envKeys) {
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

        // 判断是否存在log-agent
        for (int i = 0; i < containersJson.size(); i++) {
            if (LOG_AGENT_CONTAINER_NAME.equals(containersJson.getJSONObject(i).getString("name"))) {
                log.warn("setLogAgent log-agent container is exist");
                return;
            }
        }

        // 获取业务应用的volumeMounts，需要将log-agent的volumeMounts设置为与主容器相同的目录
        List<EnvVar> envs = new ArrayList<>();
        VolumeMount volumeMount = getVolumeMountAndAddEnvOnZheli(container, envs, admissionRequest.getString("name"));
        if (volumeMount == null) {
            log.warn("setLogAgent volume mounts is null");
            return;
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
                    envs.addAll(copyEnvForLogAgentOnZheli(env));
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
        container.setImage(System.getenv(LOG_AGENT_IMAGE_ENV_KEY) == null ? DEFAULT_LOG_AGENT_IMAGE : System.getenv(LOG_AGENT_IMAGE_ENV_KEY));

        Limits limits = new Limits();
        limits.setCpu(LOG_AGENT_RESOURCE_CPU_LIMITS);
        limits.setMemory(LOG_AGENT_RESOURCE_MEMORY_LIMITS);
        Requests requests = new Requests();
        requests.setCpu(LOG_AGENT_RESOURCE_CPU_REQUESTS);
        requests.setMemory(LOG_AGENT_RESOURCE_MEMORY_REQUESTS);
        Resource resource = new Resource();
        resource.setLimits(limits);
        resource.setRequests(requests);
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

    private VolumeMount copyVolumeForLogAgentOnZheli(JSONObject volumeMountJson, List<EnvVar> envs, String
            podName) {
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setName(volumeMountJson.getString("name"));
        volumeMount.setMountPath(volumeMountJson.getString("mountPath"));
//        volumeMount.setSubPathExpr(volumeMountJson.getString("subPathExpr"));

        // zheli定制，将subPathExpr按照logAgentConditionEnvs顺序取值拼接，除了POD_NAME，不能出现引用，不能出现空值
        String k8sNamespace = getEnvValue("K8S_NAMESPACE", envs);
        String k8sService = getEnvValue("K8S_SERVICE", envs);
        String k8sLanguage = getEnvValue("K8S_LANGUAGE", envs);
        if (StringUtils.isEmpty(k8sNamespace) || StringUtils.isEmpty(k8sService) || StringUtils.isEmpty(k8sLanguage)) {
            log.warn("setLogAgent env k8sNamespace or k8sService or k8sLanguage is empty");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(k8sNamespace).append("/").append(k8sService).append("/").append("$(POD_NAME)").append("/").append(k8sLanguage);
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
        if (logAgentConditionEnvs.isEmpty()) {
            return false;
        }
        JSONArray env = container.getJSONArray("env");
        if (env == null || env.isEmpty()) {
            return false;
        }
        Set<String> envNames = new HashSet<>();
        for (int j = 0; j < env.size(); j++) {
            envNames.add(env.getJSONObject(j).getString("name"));
        }
        return envNames.containsAll(logAgentConditionEnvs);
    }

    private boolean includeVolumeMounts(JSONObject container) {
        if (logAgentConditionVolumeMountNames.isEmpty()) {
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
        if (envsJson != null && !envsJson.isEmpty()) {
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

    private List<EnvVar> copyEnvForLogAgentOnZheli(JSONArray envsJson) {
        List<EnvVar> envs = new ArrayList<>();
        if (envsJson != null && !envsJson.isEmpty()) {
            for (int i = 0; i < envsJson.size(); i++) {
                JSONObject envJson = envsJson.getJSONObject(i);
                String name = envJson.getString("name");
                // POD_NAME单独手动设置，确保他有值
                if (logAgentConditionEnvs.contains(name) && !"POD_NAME".equals(name)) {
                    EnvVar env = JSON.toJavaObject(envJson, EnvVar.class);
                    envs.add(env);
                }
            }
        }
        // 单独设置POD_NAME，这是因为在volumeMounts中，subPathExpr目前只有一个POD_NAME是引用变量。所以必须确保log-agent有POD_NAME这个env
        envs.add(buildEnvRef("POD_NAME", "v1", "metadata.name"));
        return envs;
    }

    private EnvVar createPodIdEnv() {
        return buildEnvRef(HOST_IP, "v1", "status.podIP");
    }

    private EnvVar createNodeIdEnv() {
        return buildEnvRef(NODE_IP, "v1", "status.hostIP");
    }


    private EnvVar createPodIpCADEnv() {
        return buildEnvRef(POD_IP_CAD, "v1", "status.podIP");
    }

    private EnvVar createNodeIpCADEnv() {
        return buildEnvRef(NODE_IP_CAD, "v1", "status.hostIP");
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
        if (envs != null && !envs.isEmpty()) {
            for (int i = 0; i < envs.size(); i++) {
                keySet.add(envs.getJSONObject(i).getString("name"));
            }
        }
        return keySet;
    }

    private boolean filterByPodName(JSONObject admissionRequest) {
        if (podPrefixes.isEmpty()) {
            return false;
        }
        // 按podName前缀过滤
        String name = getPodName(admissionRequest);
        if (StringUtils.isNotEmpty(name)) {
            for (String prefix : podPrefixes) {
                if (name.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getPodName(JSONObject admissionRequest) {
        String name = admissionRequest.getString("name");
        if (StringUtils.isEmpty(name)) {
            JSONObject metadata = admissionRequest.getJSONObject("object").getJSONObject("metadata");
            name = metadata.getString("generateName");
        }
        log.info("get pod name is : " + name);
        return name;
    }

    private boolean includeNameSpace(String namespace) {
        if (StringUtils.isEmpty(namespace) || logAgentConditionNameSpaces.isEmpty()) {
            return false;
        }
        return logAgentConditionNameSpaces.contains(namespace);
    }

    private TpcAppInfo getAppInfo(JSONArray envs, String appLabelValue) {
        // 先获取K8S_SERVICE的值，这个是完整的name
        if (StringUtils.isNotEmpty(appLabelValue)) {
            TpcAppInfo appInfo = CACHE.asMap().get(appLabelValue);
            if (appInfo == null) {
                appInfo = new TpcAppInfo();
                String k8sEnv = getEnv(envs, K8S_ENV);
                String k8sCountry = getEnv(envs, K8S_APP_COUNTRY);
                String k8sService = getEnv(envs, K8S_SERVICE);
                if (StringUtils.isNotEmpty(k8sEnv) && StringUtils.isNotEmpty(k8sCountry) && StringUtils.isNotEmpty(k8sService)) {
                    getAppNameFromTpc(k8sEnv, k8sCountry, k8sService, appInfo);
                    getEnvFromTpc(k8sEnv, appInfo);
                    CACHE.put(appLabelValue, appInfo);
                } else {
                    log.warn("K8S_ENV or K8S_APP_COUNTRY or K8S_SERVICE");
                }
            }
            return appInfo;
        }
        return null;
    }

    private void getAppNameFromTpc(String k8sEnv, String k8sCountry, String k8sService, TpcAppInfo appInfo) {

        String resp = HttpClientUtil.sendPostRequest(TPC_URL, TPC_APP_REQUEST_BODY, TPC_HEADER);

        try {
            if (StringUtils.isNotEmpty(resp)) {
                JSONObject jsonObject = JSONObject.parseObject(resp);
                Integer code = jsonObject.getInteger("code");
                if (code == 0) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    if (data != null) {
                        JSONArray list = data.getJSONArray("list");
                        if (list != null) {
                            for (int i = 0; i < list.size(); i++) {
                                JSONObject node = list.getJSONObject(i);
                                Long appId = node.getLong("outId") == null || node.getLong("outId") == 0 ? node.getLong("id") : node.getLong("outId");
                                String appName = node.getString("nodeName");
                                if (appId == null || appId == 0 || StringUtils.isEmpty(appName)) {
                                    log.warn("get appName from tpc is null, appId : " + appId + " appName : " + appName);
                                } else {
                                    String k8sServiceNew = APP_PREFIX + "-" + appName + "-" + k8sCountry + "-" + k8sEnv;
                                    String k8sServiceProd = APP_PREFIX + "-" + appName + "-" + k8sCountry;
                                    if (k8sService.equals(k8sServiceNew) || k8sService.equals(k8sServiceProd)) {
                                        appInfo.setId(node.getLong("id"));
                                        appInfo.setOutId(node.getLong("outId"));
                                        appInfo.setName(appName);
                                        appInfo.setIdAndName(appId + "-" + appName);
                                        return;
                                    }else{
                                        log.warn("env k8sService : "+k8sService+" is not equals "+k8sServiceNew+" or "+k8sServiceProd);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            log.error("get appName parse tpc resp error, ", t);
        }
    }

    private void getEnvFromTpc(String k8sEnv, TpcAppInfo tpcAppInfo) {
        String envBody = "{\"parentId\":" + tpcAppInfo.getId() + ",\"type\":6,\"status\":0,\"token\":\"" + TPC_TOKEN + "\"}";
        String resp = HttpClientUtil.sendPostRequest(TPC_URL, envBody, TPC_HEADER);

        try {
            if (StringUtils.isNotEmpty(resp)) {
                JSONObject jsonObject = JSONObject.parseObject(resp);
                Integer code = jsonObject.getInteger("code");
                if (code == 0) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    if (data != null) {
                        JSONArray list = data.getJSONArray("list");
                        if (list != null) {
                            for (int i = 0; i < list.size(); i++) {
                                JSONObject node = list.getJSONObject(i);
                                Long envId = node.getLong("id");
                                String envName = node.getString("nodeName");
                                if (envId == null || envId == 0 || StringUtils.isEmpty(envName)) {
                                    log.warn("get env from tpc is null, envId : " + envId + " envName : " + envName);
                                } else {
                                    if (k8sEnv.equals(envName)) {
                                        tpcAppInfo.setEnvId(envId);
                                        tpcAppInfo.setEnvName(envName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            log.error("get env parse tpc resp error, ", t);
        }
    }

    private String getEnv(JSONArray envs, String envKey) {
        if (envs != null && StringUtils.isNotEmpty(envKey)) {
            for (int i = 0; i < envs.size(); i++) {
                JSONObject envJson = envs.getJSONObject(i);
                if (envKey.equals(envJson.getString("name")))
                    return envJson.getString("value");
            }
        }
        return null;
    }
}
