package com.trybe.modulerecommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.trybe.modulerecommendation", "com.trybe.moduleapi.challenge.dto", "com.trybe.modulecore"})
public class ModuleRecommendationApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "application,application-core");
		SpringApplication.run(ModuleRecommendationApplication.class, args);
	}

}
