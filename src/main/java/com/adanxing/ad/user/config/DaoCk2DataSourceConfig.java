package com.adanxing.ad.user.config;


import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.adanxing.ad.user.daock2", sqlSessionTemplateRef = DaoCk2DataSourceConfig.TEMPLATE_SQL_SESSION)
public class DaoCk2DataSourceConfig {

    public static final String TEMPLATE_SQL_SESSION = "analysisCk2SqlSessionTemplate";

    public static final String FACTORY_SQL_SESSION = "analysisCk2SqlSessionFactory";

    public static final String DATASOURCE = "analysisCk2DataSource";

    public static final String CONFIGRUATION = "analysisCk2Configuration";

    public static final String MAPPER_PACKAGE = "classpath:mapper/ck2/**.xml";

    @Bean(name = DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.analysisck2")
    public DataSource adminDataSource() {
        return new DruidDataSource();
    }

    @Bean(name = "analysisCk2TransactionManager")
    public DataSourceTransactionManager testTransactionManager(@Qualifier(DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


}