package com.adanxing.ad.user.controller;

import com.adanxing.ad.user.job.UserDeviceExtClearJob;
import com.adanxing.ad.user.model.R;
import com.adanxing.ad.user.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@Slf4j
@RequestMapping("data")
public class DataController {

    @Autowired
    JedisPoolConfig jedisPoolConfig;

    @RequestMapping("syncStaticsData")
    @ResponseBody
    public String syncStaticsData(String redis, String key, Integer size, Integer maxCount, Double randomRate) {
        if (StringUtils.isBlank(redis) || !StringUtils.equals(key, "123567") || Objects.isNull(size)) {
            return "false";
        }
        log.info("[DataController] syncStaticsData start, key={}, redis={}, size={}, maxCount={}, randomRate={}", key, redis, size, maxCount, randomRate);
        String[] redisArray = redis.split("\\:");
        AtomicInteger keyLength = new AtomicInteger();
        AtomicInteger totalMemoryUsage = new AtomicInteger();
        AtomicInteger totalCount = new AtomicInteger();
        AtomicInteger totalLength = new AtomicInteger();
        Map<String, Map<String, Long>> dataMap = new HashMap<>();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisArray[0], Integer.parseInt(redisArray[1]), 1000, "Anxiang861");
        try (Jedis jedis = jedisPool.getResource()) {
            UserDeviceExtClearJob userDeviceExtClearJob = UserDeviceExtClearJob.builder().jedis(jedis).cursor("0").maxCount(maxCount).randomRate(randomRate).build();
            R result = userDeviceExtClearJob.doClearRedis();
            log.info("[DataController] syncStaticsData, result={}", result);
//            String cursor = "0";
//            while(true) {
//                ScanResult<String> scanResult = jedis.scan(cursor);
//                List<String> resultList = scanResult.getResult();
//                if (CollectionUtils.isEmpty(resultList)) {
//                    break;
//                }
//                if (totalCount.addAndGet(resultList.size()) >= size) {
//                    break;
//                }
//                cursor = scanResult.getCursor();
//                resultList.stream().forEach(e -> {
//                    keyLength.addAndGet(key.getBytes().length);
//                    Map<byte[], byte[]> keyMap = jedis.hgetAll(e.getBytes());
//                    if (MapUtils.isEmpty(keyMap)) {
//                        return;
//                    }
//                    keyMap.entrySet().stream().forEach(data -> {
//                        String field = String.valueOf(ByteUtils.byteToInt(data.getKey(), 0));
//                        Map<String, Long> valueMap = dataMap.getOrDefault(field, new HashMap<>());
//                        long dataCount = valueMap.getOrDefault("count",0L);
//                        valueMap.put("count", dataCount+1);
//                        long totalSize = valueMap.getOrDefault("total_size", 0L);
//                        valueMap.put("total_size", totalSize + data.getValue().length);
//                        totalLength.getAndAdd(data.getValue().length);
//                        dataMap.put(field, valueMap);
//                    });
//                });
//            }
//            dataMap.entrySet().stream().forEach(e -> {
//                Map<String, Long> valueMap = dataMap.get(e.getKey());
//                if (Objects.isNull(valueMap)) {
//                    return;
//                }
//                long dataCount = valueMap.get("count");
//                long totalSize = valueMap.get("total_size");
//                log.info("[DataController] syncStaticsData, key={}, totalCount={}, countRate={}, size={}, sizeRate={}", e.getKey(),
//                        dataCount, new BigDecimal(dataCount).divide(new BigDecimal(totalCount.get()), 4, RoundingMode.HALF_UP).doubleValue(),
//                        totalSize, new BigDecimal(totalSize).divide(new BigDecimal(totalLength.get()), 4, RoundingMode.HALF_UP).doubleValue());
//            });
        };
        log.info("[DataController] syncStaticsData finish, key={}, totalCount={}, keyLength={}, totalMemoryUsage={}, totalLenght={}, dataMap={}", key, totalCount.get(), keyLength.get(), totalMemoryUsage.get(), totalLength.get(), JSONUtils.toJSONString(dataMap));
        return "totalCount=" + totalCount.get() + ",totalLength=" + totalLength.get() + ",totalMemoryUsage=" + totalMemoryUsage.get() + ",dataMap=" + JSONUtils.toJSONString(dataMap);
    }

}
