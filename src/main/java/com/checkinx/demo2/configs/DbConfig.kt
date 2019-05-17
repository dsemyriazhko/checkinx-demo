package com.checkinx.demo2.configs

import javax.sql.DataSource

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate

import com.checkinx.utils.asserts.CheckInxAssertService
import com.checkinx.utils.asserts.impl.CheckInxAssertServiceImpl
import com.checkinx.utils.configs.DataSourceWrapper
import com.checkinx.utils.sql.interceptors.SqlInterceptor
import com.checkinx.utils.sql.interceptors.postgres.PostgresInterceptor
import com.checkinx.utils.sql.plan.parse.ExecutionPlanParser
import com.checkinx.utils.sql.plan.parse.impl.PostgresExecutionPlanParser
import com.checkinx.utils.sql.plan.query.ExecutionPlanQuery
import com.checkinx.utils.sql.plan.query.impl.PostgresExecutionPlanQuery
import net.ttddyy.dsproxy.support.ProxyDataSource

@Configuration
open class DbConfig {
    @Profile("test")
    @Bean
    open fun dataSourceWrapperBeanPostProcessor(): DataSourceWrapper {
        return DataSourceWrapper()
    }

    @Profile("test")
    @Bean
    open fun sqlInterceptor(dataSource: DataSource): SqlInterceptor {
        return PostgresInterceptor(dataSource as ProxyDataSource)
    }

    @Profile("test")
    @Bean
    open fun executionPlanParser(): ExecutionPlanParser {
        return PostgresExecutionPlanParser()
    }

    @Profile("test")
    @Bean
    open fun executionPlanQuery(jdbcTemplate: JdbcTemplate): ExecutionPlanQuery {
        return PostgresExecutionPlanQuery(jdbcTemplate)
    }

    @Profile("test")
    @Bean
    open fun checkInxAssertService(query: ExecutionPlanQuery, parser: ExecutionPlanParser): CheckInxAssertService {
        return CheckInxAssertServiceImpl(query, parser)
    }
}
