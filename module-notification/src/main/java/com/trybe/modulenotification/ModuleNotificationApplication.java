package com.trybe.modulenotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.trybe.modulenotification", "com.trybe.moduleapi.notification.dto", "com.trybe.modulecore.notification"})
public class ModuleNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleNotificationApplication.class, args);
	}

}
