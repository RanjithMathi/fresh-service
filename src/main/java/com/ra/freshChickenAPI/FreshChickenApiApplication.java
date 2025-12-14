package com.ra.freshChickenAPI;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ra.freshChickenAPI.service.AdminAuthService;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableScheduling
public class FreshChickenApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreshChickenApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner createDefaultAdmin(AdminAuthService adminAuthService) {
		return args -> {
			try {
				// Create default admin if not exists
				adminAuthService.createAdmin("admin", "admin123", "Default Admin", "ADMIN");
				System.out.println("Default admin created with username: admin, password: admin123");
			} catch (Exception e) {
				System.out.println("Default admin already exists or error creating: " + e.getMessage());
			}
		};
	}

}
