package run.mone.hera.webhook.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.ObjectFieldSelector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import run.mone.hera.webhook.domain.*;

import java.util.*;

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
    private static final String LOG_AGENT_IMAGE = "herahub/opensource-pub:log-agent-v1-release";
    private static final String LOG_AGENT_NACOS_ADDR = "nacos.hera-namespace:80";
    private static final String LOG_AGENT_NACOS_ENV_KEY = "nacosAddr";
    private static final String LOG_AGENT_VOLUME_MOUNTS_NAME = "app-logs";
    private static final List<String> LOG_AGENT_VOLUME_MOUNTS_ENVS = Arrays.asList(new String[]{"K8S_NAMESPACE","K8S_SERVICE","POD_NAME","K8S_LANGUAGE"});

    /**
     * 需要添加log-agent container的pod名称前缀
     */
    private static final String LOG_AGENT_POD_PREFIX = "hera-demo";

    public List<JsonPatch> setPodEnv(JSONObject admissionRequest) {
        String operation = admissionRequest.getString("operation");
        switch (operation) {
            case "CREATE":
            case "UPDATE":
                List<JsonPatch> result = new ArrayList<>();
                JSONArray containersJson = admissionRequest.getJSONObject("object").getJSONObject("spec").getJSONArray("containers");
                for (int i = 0; i < containersJson.size(); i++) {
                    JSONObject container = containersJson.getJSONObject(i);
                    if (container != null) {
                        JSONArray env = container.getJSONArray("env");
                        // don't have env element
                        if (env == null) {
                            String path = "/spec/containers/" + i + "/env";
                            List<EnvVar> envs = new ArrayList<>();
                            envs.add(createPodIdEnv());
                            envs.add(createNodeIdEnv());
                            result.add(new JsonPatch("add", path, envs));
                        } else {
                            Set<String> envKeys = envKeys(env);
                            String path = "/spec/containers/" + i + "/env/-";
                            if (!envKeys.contains(HOST_IP)) {
                                result.add(new JsonPatch("add", path, createPodIdEnv()));
                            }
                            if (!envKeys.contains(NODE_IP)) {
                                result.add(new JsonPatch("add", path, createNodeIdEnv()));
                            }
                            //set cadvisor need env
                            if (env != null && env.size() > 0) {
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
                        }
                    }
                }
                return result;
            default:
                return null;
        }
    }

    public void setLogAgent(JSONObject admissionRequest, List<JsonPatch> jsonPatches){
        // 过滤
        String name = admissionRequest.getString("name");
        if(StringUtils.isEmpty(name)){
            JSONObject metadata = admissionRequest.getJSONObject("object").getJSONObject("metadata");
            name = metadata.getString("generateName");
        }
        log.info("setLogAgent get pod name is : "+name);
        if(StringUtils.isEmpty(name) || !name.startsWith(LOG_AGENT_POD_PREFIX)){
            return;
        }
        String operation = admissionRequest.getString("operation");
        switch (operation) {
            case "CREATE":
                if(jsonPatches == null){
                    jsonPatches = new ArrayList<>();
                }
                JSONArray containersJson = admissionRequest.getJSONObject("object").getJSONObject("spec").getJSONArray("containers");
                // 判断是否存在log-agent，以及获取主容器挂载出来的日志路径信息
                VolumeMount volumeMount;
                List<EnvVar> envs = new ArrayList<>();
                boolean isBuild = false;
                for(int i = 0; i < containersJson.size(); i++){
                    JSONObject container = containersJson.getJSONObject(i);
                    String containerName = container.getString("name");
                    if(LOG_AGENT_CONTAINER_NAME.equals(containerName)){
                        return;
                    }
                    // 获取业务应用的volumeMounts，需要将log-agent的volumeMounts设置为与主容器相同的目录
                    JSONArray volumeMounts = container.getJSONArray("volumeMounts");
                    if(volumeMounts != null && volumeMounts.size() > 0){
                        for(int j = 0; j< volumeMounts.size(); j++) {
                            JSONObject volumeMountJson = volumeMounts.getJSONObject(j);
                            String volumeMountName = volumeMountJson.getString("name");
                            if (LOG_AGENT_VOLUME_MOUNTS_NAME.equals(volumeMountName)){
                                volumeMount = copyVolumnForLogAgent(volumeMountJson);
                                isBuild = true;
                                // 复制env

                                envs.add()
                            }
                        }
                    }
                }
                // 如果满足条件， 则新建log-agent容器
                if(isBuild) {
                    String path = "/spec/containers/-";
                    Container container = new Container();
                    container.setName(LOG_AGENT_CONTAINER_NAME);
                    container.setImage(LOG_AGENT_IMAGE);
                    Limits limits = new Limits();
                    limits.setCpu("300m");
                    limits.setMemory("1Gi");
                    Resource resource = new Resource();
                    resource.setLimits(limits);
                    container.setResources(resource);
                    jsonPatches.add(new JsonPatch<>("add", path, container));
                    log.info("log agent added");
                }
        }
    }

    private VolumeMount copyVolumnForLogAgent(JSONObject volumeMountJson){
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setName(LOG_AGENT_VOLUME_MOUNTS_NAME);
        volumeMount.setMountPath(volumeMountJson.getString("mountPath"));
        volumeMount.setSubPathExpr(volumeMountJson.getString("subPathExpr"));
        return volumeMount;
    }

    private List<EnvVar> copyEnvForLogAgent(JSONArray envsJson){
        if(envsJson != null && envsJson.size() > 0){
            List<EnvVar> envs = new ArrayList<>();
            for(int i = 0;i<envsJson.size();i++){
                JSONObject envJson = envsJson.getJSONObject(i);
                String name = envJson.getString("name");
                if(LOG_AGENT_VOLUME_MOUNTS_ENVS.contains(name))
                EnvVar env = new EnvVar();
                env.setName(envJson.getString("name"));

            }
        }
    }

    private EnvVar createPodIdEnv(){
        return buildEnvRef(HOST_IP, "v1", "status.podIP");
    }
    private EnvVar createNodeIdEnv(){
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

}
