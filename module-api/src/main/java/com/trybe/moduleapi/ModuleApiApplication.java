package com.trybe.moduleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.trybe.moduleapi", "com.trybe.modulecore"})
public class ModuleApiApplication {

    public static void main(String[] args) {
        System.setProperty("spring.config.name", "application,application-core");
        SpringApplication.run(ModuleApiApplication.class, args);
    }

}
