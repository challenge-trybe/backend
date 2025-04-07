package com.trybe.modulerecommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.trybe.modulerecommendation", "com.trybe.moduleapi.challenge.dto", "com.trybe.modulecore"})
public class ModuleRecommendationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleRecommendationApplication.class, args);
	}

}
