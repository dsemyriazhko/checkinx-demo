package com.checkinx.demo2.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.checkinx.utils.configs.DataSourceWrapper;

@Configuration
public class DataSourceConfig {
    @Profile("test")
    @Bean
    public DataSourceWrapper dataSourceWrapperBeanPostProcessor() {
        return new DataSourceWrapper();
    }
}
