package com.checkinx.demo2.configs

import com.checkinx.utils.configs.PostgresConfig
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@ImportAutoConfiguration(classes = [PostgresConfig::class])
@Configuration
open class CheckInxConfig
