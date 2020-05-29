package com.loujinhe.droolsdynamicloading.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;

@Configuration
@MapperScan(basePackages = DataSourceConfig.PACKAGE, sqlSessionFactoryRef = "SqlSessionFactory")
public class DataSourceConfig {
    static final String PACKAGE = "com.loujinhe.droolsdynamicloading.dao";


//    @Primary
//    @Bean(name = "DataSource")
//    @ConfigurationProperties(prefix="spring.datasource")
//    public DataSource DataSource() {
//        return DataSourceBuilder.create().build();
//    }

//    @Bean(name = "TransactionManager")
//    @Primary
//    public DataSourceTransactionManager dbTransactionManager() {
//        return new DataSourceTransactionManager(DataSource());
//    }

    @Bean(name = "SqlSessionFactory")
    public SqlSessionFactory dbSqlSessionFactory(DataSource DataSource) throws Exception {
        final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(DataSource);
        return sessionFactory.getObject();
    }
}