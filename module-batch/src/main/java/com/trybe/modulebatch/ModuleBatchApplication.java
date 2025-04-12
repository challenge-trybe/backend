package com.trybe.modulebatch;

import com.trybe.moduleapi.config.RedisConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"com.trybe.modulebatch", "com.trybe.modulecore"})
@Import(RedisConfig.class)
public class ModuleBatchApplication {
	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(ModuleBatchApplication.class);
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		ConfigurableApplicationContext context = springApplication.run(args);
		int exitCode = SpringApplication.exit(context);
		System.exit(exitCode);
	}
}
