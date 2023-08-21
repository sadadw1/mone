package com.xiaomi.mone.app.service.env;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.mone.app.common.Result;
import com.xiaomi.mone.app.common.TpcLabelRes;
import com.xiaomi.mone.app.common.TpcPageRes;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.xiaomi.mone.app.common.Constant.DEFAULT_REGISTER_REMOTE_TYPE;
import static com.xiaomi.mone.app.common.Constant.URL.HERA_TPC_APP_DETAIL_URL;

/**
 * @author wtt
 * @version 1.0
 * @description 根据配置文件开关选择合适的获取配置环境的实现类
 * @date 2022/11/29 16:54
 */
@Service
@Slf4j
public class DefaultEnvIpFetch {

    @Autowired
    private DefaultHttpEnvIpFetch defaultHttpEnvIpFetch;

    @Autowired
    private DefaultNacosEnvIpFetch defaultNacosEnvIpFetch;

    @Value("${app.ip.fetch.type}")
    private String envApppType;

    @NacosValue("${hera.tpc.url}")
    private String heraTpcUrl;

    @Resource
    private OkHttpClient okHttpClient;

    @Autowired
    private Gson gson;


    public EnvIpFetch getEnvIpFetch() {
        if (Objects.equals(EnvIpTypeEnum.HTTP.name().toLowerCase(), envApppType)) {
            return defaultHttpEnvIpFetch;
        }
        return defaultNacosEnvIpFetch;
    }

    public EnvIpFetch getEnvFetch(String appId) {
        EnvIpFetch fetchFromRemote = getEnvFetchFromRemote(appId);
        if (null != fetchFromRemote) {
            return fetchFromRemote;
        }
        return getEnvIpFetch();
    }

    private EnvIpFetch getEnvFetchFromRemote(String appId) {
        String url = heraTpcUrl + HERA_TPC_APP_DETAIL_URL;
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("parentId", appId);
        jsonObject.addProperty("flagKey", DEFAULT_REGISTER_REMOTE_TYPE);
        RequestBody requestBody = RequestBody.create(mediaType, gson.toJson(jsonObject));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = null;
        ResponseBody body = null;
        try {
            response = okHttpClient.newCall(request).execute();
            log.info("url is : "+url+" appId : "+appId+" get mi-tpc response is success: "+response.isSuccessful() + "response : "+response.toString());
            if (response.isSuccessful()) {
                body = response.body();
                String rstJson = body.string();
                log.info("appId : "+appId+" get mi-tpc response : "+rstJson);
                Result<TpcPageRes<TpcLabelRes>> pageResponseResult = gson.fromJson(rstJson, new TypeToken<Result<TpcPageRes<TpcLabelRes>>>() {
                }.getType());
                TpcPageRes<TpcLabelRes> data = pageResponseResult.getData();
                if(data != null){
                    List<TpcLabelRes> list = data.getList();
                    if(list != null){
                        for (TpcLabelRes tpcLabelRes : pageResponseResult.getData().getList()) {
                            if (Objects.equals(Boolean.TRUE.toString(), tpcLabelRes.getFlagVal())) {
                                return defaultHttpEnvIpFetch;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("getEnvFetchFromRemote error,appId:{}", appId, e);
        }finally {
            if(response != null){
                response.close();
            }
            if(body != null){
                body.close();
            }
        }
        return null;
    }


    public static enum EnvIpTypeEnum {
        NACOS, HTTP;
    }
}
