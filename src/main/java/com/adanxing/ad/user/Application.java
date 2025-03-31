package com.adanxing.ad.user;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@EnableTransactionManagement
@EnableApolloConfig(value = {"application", "server.jedis_cluster.config"})
@SpringBootApplication(scanBasePackages = "com.adanxing.ad.user")
@EnableAsync
@Slf4j
public class Application {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        new SpringApplication(Application.class).run(args);
        log.info("ad-user start costTime is {}", (System.currentTimeMillis() - startTime));
    }
}