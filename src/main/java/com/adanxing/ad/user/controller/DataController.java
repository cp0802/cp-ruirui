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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@Slf4j
@RequestMapping("data")
public class DataController {

    @RequestMapping("syncStaticsData")
    @ResponseBody
    public String syncStaticsData(String redis, String key, Integer size, Integer maxCount, Double randomRate) {
        if (StringUtils.isBlank(redis) || !StringUtils.equals(key, "123567") || Objects.isNull(size)) {
            return "false";
        }
        log.info("[DataController] syncStaticsData start, key={}, redis={}, size={}, maxCount={}, randomRate={}", key, redis, size, maxCount, randomRate);
        Arrays.stream(redis.split(",")).forEach(e -> {
            String[] redisArray = e.split("\\:");
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(10);
            JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisArray[0], Integer.parseInt(redisArray[1]), 1000, "Anxiang861");
            try (Jedis jedis = jedisPool.getResource()) {
                UserDeviceExtClearJob userDeviceExtClearJob = UserDeviceExtClearJob.builder().jedis(jedis).cursor("0").maxCount(maxCount).randomRate(randomRate).build();
                userDeviceExtClearJob.run();
                log.info("[DataController] syncStaticsData start, redis={}", e);
            };
        });
        return "true";
    }

}
