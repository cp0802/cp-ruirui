package com.adanxing.ad.user.service.impl;

import com.adanxing.ad.user.service.JedisClusterService;
import com.google.common.primitives.Bytes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.util.JedisClusterCRC16;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class JedisClusterServiceImpl implements JedisClusterService {

    private JedisCluster jedisCluster;

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    public JedisClusterServiceImpl(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public int hgetInt(String key, String field, int defaultValue) {
        int resultValue = defaultValue;
        try {
            String jedisValue = jedisCluster.hget(key, field);
            if (StringUtils.isNotBlank(jedisValue)) {
                resultValue = Integer.parseInt(jedisValue);
            }
        } catch (Exception e) {
            log.error("[jedis-cluster] hget error, key is {}, field is {}, defaultValue is {}", key, field, defaultValue);
        }
        return resultValue;
    }

    @Override
    public long hincr(String key, String field, int value) {
        try {
            return jedisCluster.hincrBy(key, field, value);
        } catch (Exception e) {
            log.error("[jedis-cluster] hincr error, key is {}, field is {}, value is {}", key, field, value);
        }
        return 0;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        int resultValue = defaultValue;
        try {
            String jedisValue = jedisCluster.get(key);
            if (StringUtils.isNotBlank(jedisValue)) {
                resultValue = Integer.parseInt(jedisValue);
            }
        } catch (Exception e) {
            log.error("[jedis-cluster] getInt error, key is {}, field is {}, defaultValue is {}", key, defaultValue);
        }
        return resultValue;
    }

    @Override
    public long incr(String key, int value) {
        try {
            return jedisCluster.incrBy(key, value);
        } catch (Exception e) {
            log.error("[jedis-cluster] incr error, key is {}, field is {}, value is {}", key, value);
        }
        return 0;
    }

    @Override
    public long hset(String key, String field, String value) {
        try {
            return jedisCluster.hset(key, field, value);
        } catch (Exception e) {
            log.error("[jedis-cluster] hset error, key is {}, field is {}, value is {}", key, field, value);
        }
        return 0;
    }

    @Override
    public long expireSeconds(String key, int seconds) {
        try {
            return jedisCluster.expire(key, seconds);
        } catch (Exception e) {
            log.error("[jedis-cluster] expireSeconds error, key is {}, field is {}, value is {}", key, seconds);
        }
        return 0;
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        try {
            return jedisCluster.hgetAll(key);
        } catch (Exception e) {
            log.error("[jedis-cluster] hgetall error, key is {}, error is {}", key, e);
        }
        return new HashMap<>();
    }

    @Override
    public String setex(String key, int seconds, String value) {
        try {
            return jedisCluster.setex(key, seconds, value);
        } catch (Exception e) {
            log.error("[jedis-cluster] setex error, key is {}, seconds is {}, value is {}", key, seconds, value);
        }
        return "";
    }

    @Override
    public String get(String key, String defaultValue) {
        try {
            String resultString = jedisCluster.get(key);
            return StringUtils.isBlank(resultString) ? defaultValue : resultString;
        } catch (Exception e) {
            log.error("[jedis-cluster] get error, key is {}, defaultValue is {}", key, defaultValue);
        }
        return defaultValue;
    }

    @Override
    public Map<List<Byte>, byte[]> batchHGet(Set<byte[]> keys, byte[] field) {
        Map<List<Byte>, byte[]> resultMap = new HashMap<>();
        if (keys == null || keys.isEmpty()) return resultMap;

        // 获取集群节点和槽位映射（同之前实现）
        Map<String, ConnectionPool> clusterNodes = jedisCluster.getClusterNodes();
        Map<Integer, ConnectionPool> slotPoolMap = buildSlotPoolMap(clusterNodes);

        // 按节点分组执行
        Map<ConnectionPool, List<byte[]>> poolKeysMap = new HashMap<>();
        keys.forEach(key -> {
            int slot = JedisClusterCRC16.getSlot(key);
            ConnectionPool pool = slotPoolMap.get(slot);
            if (pool != null) poolKeysMap.computeIfAbsent(pool, k -> new ArrayList<>()).add(key);
        });

        // 新版 Pipeline 处理
        poolKeysMap.forEach((pool, keyList) -> {
            try (Connection connection = pool.getResource()) {
                Pipeline pipeline = new Pipeline(connection);
                Map<byte[], Response<byte[]>> responses = new HashMap<>();
                keyList.forEach(key ->
                        responses.put(key, pipeline.hget(key, field))
                );

                pipeline.sync();

                responses.forEach((key, resp) -> {
                    byte[] value = resp.get();
                    if (value != null) resultMap.put(Bytes.asList(key), value);
                });
            } catch (Exception e) {
                log.error("Pipeline failed for node: {}", pool, e);
            }
        });

        return resultMap;
    }

    @Override
    public boolean batchHSet(Map<byte[], byte[]> keyValueMap, byte[] field) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return false;
        }

        // 1. 获取集群所有节点连接池
        Map<String, ConnectionPool> clusterNodes = jedisCluster.getClusterNodes();
        if (clusterNodes.isEmpty()) {
            return false;
        }

        // 2. 构建槽位到连接池的映射
        Map<Integer, ConnectionPool> slotPoolMap = buildSlotPoolMap(clusterNodes);

        // 3. 按节点分组键值对
        Map<ConnectionPool, Map<byte[], byte[]>> poolKvMap = new HashMap<>();
        for (Map.Entry<byte[], byte[]> entry : keyValueMap.entrySet()) {
            int slot = JedisClusterCRC16.getSlot(entry.getKey());
            ConnectionPool pool = slotPoolMap.get(slot);
            if (pool != null) {
                poolKvMap.computeIfAbsent(pool, k -> new HashMap<>())
                        .put(entry.getKey(), entry.getValue());
            }
        }

        // 4. 每个节点执行 Pipeline
        AtomicBoolean allSuccess = new AtomicBoolean(true);
        poolKvMap.forEach((pool, kvMap) -> {
            try (Connection connection = pool.getResource()) {
                Pipeline pipeline = new Pipeline(connection);
                List<Response<Long>> responses = new ArrayList<>();
                // 批量发送 HSET 命令
                kvMap.forEach((key, value) -> responses.add(pipeline.hset(key, field, value)));
                pipeline.sync();

                // 验证结果（0=更新，1=新建，其他值表示失败）
                responses.forEach(resp -> {
                    Long result = resp.get();
                    if (result != 0L && result != 1L) {
                        log.warn("HSET failed for key in pool: {}", pool);
                        allSuccess.set(false);
                    }
                });
            } catch (Exception e) {
                log.error("Pipeline HSET failed for node: {}", pool, e);
                allSuccess.set(false);
            }
        });

        return allSuccess.get();
    }

    private Map<Integer, ConnectionPool> buildSlotPoolMap(Map<String, ConnectionPool> clusterNodes) {
        Map<Integer, ConnectionPool> slotPoolMap = new HashMap<>();
        if (clusterNodes.isEmpty()) {
            return slotPoolMap;
        }

        // 通过任意节点获取集群槽位分配信息
        try (Connection connection = clusterNodes.values().iterator().next().getResource()) {
            // Jedis 5.1.5 正确的命令构建方式
            CommandObject<List<Object>> cmd = new CommandObject<>(
                    new CommandArguments(Protocol.Command.CLUSTER).add("SLOTS"),
                    BuilderFactory.RAW_OBJECT_LIST
            );
            List<Object> slotsInfo =  connection.executeCommand(cmd);

            // 解析槽位分配信息
            for (Object slotObj : slotsInfo) {
                List<Object> slotTuple = (List<Object>) slotObj;
                int startSlot = ((Long) slotTuple.get(0)).intValue();
                int endSlot = ((Long) slotTuple.get(1)).intValue();
                List<Object> masterInfo = (List<Object>) slotTuple.get(2);
                String hostPort = new String((byte[]) masterInfo.get(0)) + ":" + masterInfo.get(1);
                ConnectionPool pool = clusterNodes.get(hostPort);

                // 填充槽位范围
                if (pool != null) {
                    for (int slot = startSlot; slot <= endSlot; slot++) {
                        slotPoolMap.put(slot, pool);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to build slot-pool mapping", e);
        }

        return slotPoolMap;
    }
}
