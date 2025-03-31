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
@MapperScan(basePackages = "com.adanxing.ad.user.daock1", sqlSessionTemplateRef = DaoCk1DataSourceConfig.TEMPLATE_SQL_SESSION)
public class DaoCk1DataSourceConfig {

    public static final String TEMPLATE_SQL_SESSION = "analysisSqlSessionTemplate";

    public static final String FACTORY_SQL_SESSION = "analysisSqlSessionFactory";

    public static final String DATASOURCE = "analysisDataSource";

    public static final String CONFIGRUATION = "analysisConfiguration";

    public static final String MAPPER_PACKAGE = "classpath:mapper/ck1/**.xml";

    @Bean(name = DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.analysis")
    public DataSource adminDataSource() {
        return new DruidDataSource();
    }

    @Bean(name = CONFIGRUATION)
    @ConfigurationProperties(prefix = "mybatis.configuration")
    public org.apache.ibatis.session.Configuration mybatiesConfig() {
        return new org.apache.ibatis.session.Configuration();
    }

    @Bean(name = FACTORY_SQL_SESSION)
    public SqlSessionFactory testSqlSessionFactory(@Qualifier(DATASOURCE) DataSource dataSource, @Qualifier(CONFIGRUATION) org.apache.ibatis.session.Configuration configuration) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MAPPER_PACKAGE));
        bean.setConfiguration(configuration);
        return bean.getObject();
    }

    @Bean(name = "analysisTransactionManager")
    public DataSourceTransactionManager testTransactionManager(@Qualifier(DATASOURCE) DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = TEMPLATE_SQL_SESSION)
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier(FACTORY_SQL_SESSION) SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}