package com.adanxing.ad.user.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface JedisClusterService {

    int hgetInt(String key, String field, int defaultValue);

    long hincr(String key, String field, int value);

    int getInt(String key, int defaultValue);

    long incr(String key, int value);

    long hset(String key, String field, String value);

    long expireSeconds(String key, int seconds);

    Map<String, String> hgetAll(String key);

    String setex(String key, int seconds, String value);

    String get(String key, String defaultValue);

    Map<List<Byte>, byte[]> batchHGet(Set<byte[]> keys, byte[] field);

    boolean batchHSet(Map<byte[], byte[]> keyValueMap, byte[] field);

}
