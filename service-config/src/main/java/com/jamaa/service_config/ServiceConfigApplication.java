package com.jamaa.service_config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ServiceConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceConfigApplication.class, args);
	}

}
