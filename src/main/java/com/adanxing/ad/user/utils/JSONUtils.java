package com.adanxing.ad.user.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class JSONUtils {

    static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);

    public static <T> List<T> parseArray(String content, Class<T> tClass) {
        return JSON.parseArray(content, tClass);
    }

    public static <T> T parseObject(String content, Class<T> tClass) {
        try {
            return (T) JSON.parseObject(content, tClass);
        } catch (Exception e) {
            logger.error("parse json error, content is {}", content, e);
        }
        return null;
    }

    public static <T> T parseObject(String content, TypeReference<T> tTypeReference) {
        return JSON.parseObject(content, tTypeReference);
    }

    public static String toJSONString(Object object) {
        if (object == null) {
            return null;
        }
        return JSON.toJSONString(object);
    }

    public static Map toMap(Object object) {
        return JSON.parseObject(JSON.toJSONString(object));
    }

    public static <T> T parseObject(Map map, Class<T> tClass) {
        return JSON.parseObject(JSON.toJSONString(map), tClass);
    }
}
