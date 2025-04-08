package com.trybe.modulebatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ModuleBatchApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(ModuleBatchApplication.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		ConfigurableApplicationContext context = springApplication.run(args);
		int exitCode = SpringApplication.exit(context);
		System.exit(exitCode);
	}

}
