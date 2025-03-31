package com.adanxing.ad.user.config;

import com.adanxing.ad.user.service.JedisClusterService;
import com.adanxing.ad.user.service.impl.JedisClusterServiceImpl;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.*;
import redis.clients.jedis.providers.ClusterConnectionProvider;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;


@Configuration
public class JedisClusterConfig {

    @Value("${spring.redis.cluster.connectionTimeout:1500}")
    private Integer connectionTimeout;

    @Value("${spring.redis.cluster.soTimeout:1500}")
    private Integer soTimeout;

    @Value("${spring.redis.cluster.maxAttempts:1500}")
    private Integer maxAttempts;

    @Value("${spring.redis.cluster.password:Anxiang861}")
    private String password;

    @Value("${spring.jedisCluster.pool.max-idle:5000}")
    private int maxIdle;

    @Value("${spring.jedisCluster.pool.max-total:100000}")
    private int maxTotal;

    @Value("${spring.jedisCluster.pool.max-wait:100000}")
    private long maxWaitMillis;

    @Value("${spring.jedisCluster.block-when-exhausted:true}")
    private boolean blockWhenExhausted;

    @Value("${ad.device.ext.new.cluster.nodes}")
    private String deviceExtNewClusterNodes;

    @Bean(name = "deviceExtNewCluster")
    public JedisCluster getDeviceExtNewCluster() {
        // 1. 节点配置
        Set<HostAndPort> nodes = parseClusterNodes(deviceExtNewClusterNodes);

        // 2. 客户端配置 (Jedis 5.x 新API)
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .password(password)
                .timeoutMillis(soTimeout)
                .connectionTimeoutMillis(connectionTimeout)
                .build();

        // 3. 连接池配置
        GenericObjectPoolConfig<Connection> poolConfig = jedisPoolConfig();
        // 4. 构建 ClusterConnectionProvider (Jedis 5.x 核心变更)
        ClusterConnectionProvider provider = new ClusterConnectionProvider(nodes, clientConfig, poolConfig);

        // 5. 创建 JedisCluster 实例
        return new JedisCluster(provider, maxAttempts, Duration.ofMillis(soTimeout));
    }

    @Bean(name = "deviceExtNewClusterService")
    @Singleton
    public JedisClusterService deviceExtNewClusterService(@Qualifier("deviceExtNewCluster") JedisCluster jedisCluster) {
        return new JedisClusterServiceImpl(jedisCluster);
    }
    private Set<HostAndPort> parseClusterNodes(String nodesStr) {
        Set<HostAndPort> nodes = new HashSet<>();
        for (String node : nodesStr.split(",")) {
            String[] parts = node.split(":");
            nodes.add(new HostAndPort(parts[0].trim(), Integer.parseInt(parts[1].trim())));
        }
        return nodes;
    }

    private GenericObjectPoolConfig<Connection> jedisPoolConfig() {
        GenericObjectPoolConfig<Connection> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(this.maxTotal);
        config.setMaxIdle(this.maxIdle);
        config.setMaxWait(Duration.ofMillis(this.maxWaitMillis));
        config.setBlockWhenExhausted(this.blockWhenExhausted);
        config.setMinIdle(10);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setJmxEnabled(true);
        config.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));

        return config;
    }

}

