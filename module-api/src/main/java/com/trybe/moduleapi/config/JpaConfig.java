package com.trybe.moduleapi.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.trybe.modulecore")
@EntityScan(basePackages = "com.trybe.modulecore")
public class JpaConfig {
}
