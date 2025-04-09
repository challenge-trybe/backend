package com.trybe.moduleapi.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("com.trybe.moduleapi.challenge")
public class FeignConfig {
}
