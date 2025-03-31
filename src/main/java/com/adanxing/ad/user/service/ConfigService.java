package com.adanxing.ad.user.service;

import com.adanxing.ad.user.utils.JSONUtils;
import com.alibaba.fastjson.TypeReference;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConfigService {

    @ApolloConfig
    Config config;

    private Map<String,Object> PROPERTY_MAP=new HashMap<>();

    public <T> T getBean(String key , String defaultValue, Class<T> tClass) {
        String json = config.getProperty(key, defaultValue);
        return StringUtils.isNotBlank(json) ? com.adanxing.ad.api.util.JSONUtils.parseObject(json, tClass) : null;
    }

    public <T> T getBean(String key, String defaultValue, TypeReference<T> tTypeReference) {

        String json = config.getProperty(key, defaultValue);
        if (StringUtils.isBlank(json)) {
            return null;
        }
        T data = JSONUtils.parseObject(json, tTypeReference);
        return data;


    }

    public <T> List<T> getList(String key, String defaultValue, Class<T> tClass) {
        String json = config.getProperty(key, defaultValue);
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        return JSONUtils.parseArray(json, tClass);
    }

    public String getConfigValue(String key, String defaultValue) { return config.getProperty(key, defaultValue); }

    public boolean getConfigBooleanValue(String key, boolean defaultValue) {
        return config.getBooleanProperty(key, defaultValue);
    }

    public Integer getConfigIntValue(String key, Integer defaultValue) {
        return config.getIntProperty(key, defaultValue);
    }
    public Double getConfigDoubleValue(String key, Double defaultValue){
        return config.getDoubleProperty(key,defaultValue);
    }
    public <T> T getBeanFromLocalCache(String key, String defaultValue, TypeReference<T> tTypeReference) {
        Object dataCache=PROPERTY_MAP.get(key);;
        if(Objects.isNull(dataCache)){
            String json = config.getProperty(key, defaultValue);
            if (StringUtils.isBlank(json)) {
                return null;
            }
            T data = JSONUtils.parseObject(json, tTypeReference);
            PROPERTY_MAP.put(key,data);
            return data;
        }
        return (T) dataCache;
    }
}
