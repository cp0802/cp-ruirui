package com.adanxing.ad.user.job;

import com.adanxing.ad.device.enums.DeviceExtEnum;
import com.adanxing.ad.device.proto.AdanxingDeviceProto;
import com.adanxing.ad.device.utils.DeviceExtUtils;
import com.adanxing.ad.user.model.R;
import com.adanxing.ad.user.utils.DateUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Builder
public class UserDeviceExtClearJob {

    private Jedis jedis;
    private String cursor;
    private Integer maxCount;
    private Double randomRate;
    final AtomicInteger totalCount = new AtomicInteger();
    final Map<DeviceExtEnum, Map<String, Long>> memoryUsageTypeMap = new HashMap<>();
    static final Random RANDOM = new Random();

    public R doClearRedis() {
        log.info("[UserDeviceExtClearJob] doClearRedis start, maxCount={}, randomRate={}", maxCount, randomRate);
        while(true) {
            ScanResult<String> scanResult = jedis.scan(cursor);
            List<String> keyList = scanResult.getResult();
            if (CollectionUtils.isEmpty(keyList)) {
                break;
            }
            this.cursor = scanResult.getCursor();
            keyList.stream().forEach(key -> {
                Map<byte[], byte[]> fieldMap = jedis.hgetAll(key.getBytes());
                if (MapUtils.isEmpty(fieldMap)) {
                    return;
                }
                fieldMap.entrySet().stream().forEach(fieldData -> {
                    dealProperties(key, fieldData);
                });
            });
            if (Objects.nonNull(maxCount) && maxCount > 0 && totalCount.addAndGet(keyList.size()) >= maxCount) {
                break;
            }
        }
        log.info("[UserDeviceExtClearJob] doClearRedis finish, total_count={}", totalCount.get());
        return R.ok().put("totalCount", totalCount.get());
    }

    void dealProperties(String key, Map.Entry<byte[], byte[]> fieldData) {
        DeviceExtEnum deviceExtType = DeviceExtUtils.convertDeviceExtType(fieldData.getKey());
        try {
            if (Objects.isNull(deviceExtType)) {
                return;
            }
            switch (deviceExtType) {
                case PDD_FEATURE:
                    AdanxingDeviceProto.PddFeature pddFeature = AdanxingDeviceProto.PddFeature.parseFrom(fieldData.getValue());
                    if (Objects.nonNull(pddFeature) && isDelPddFeature(pddFeature)) {
                        boolean checkFlag = checkReleaseMemoryUsage();
                        if (checkFlag) {
                            long memoryUsagePre = jedis.memoryUsage(key);
                            jedis.hdel(key.getBytes(), fieldData.getKey());
                            long memoryUsageAfter = jedis.exists(key) ? jedis.memoryUsage(key) : 0;
                            Map<String, Long> memoryUsageMap = memoryUsageTypeMap.getOrDefault(deviceExtType, new HashMap<>());
                            long totalCount = memoryUsageMap.getOrDefault("total_count", 0L) + 1;
                            memoryUsageMap.put("total_count", totalCount);
                            long memoryUsage = memoryUsageMap.getOrDefault("memory_usage", 0L) + (memoryUsageAfter - memoryUsagePre);
                            memoryUsageMap.put("memory_usage", memoryUsage);
                            memoryUsageTypeMap.put(deviceExtType, memoryUsageMap);
                            if (memoryUsageMap.get("total_count") % 10000 == 0) {
                                log.info("[UserDeviceExtClearJob] release PddFeature, total_count={}, memoryUsage={}, avgMemory={}", totalCount, memoryUsage, memoryUsage / totalCount);
                                memoryUsageTypeMap.clear();
                            }
                        } else {
                            jedis.hdel(key.getBytes(), fieldData.getKey());
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            log.error("[UserDeviceExtClearJob] dealProperties fail, deviceExtType={}", deviceExtType, e);
        }
    }

    boolean checkReleaseMemoryUsage() {
        return Objects.nonNull(randomRate) && randomRate > 0 && RANDOM.nextDouble() < randomRate;
    }

    boolean isDelPddFeature(AdanxingDeviceProto.PddFeature pddFeature) {
        if (!pddFeature.hasLastReqTimeMillions() || DateUtils.plusDay(new Date(pddFeature.getLastReqTimeMillions()), -2).before(new Date())) {
            return true;
        }
        return false;
    }
}
