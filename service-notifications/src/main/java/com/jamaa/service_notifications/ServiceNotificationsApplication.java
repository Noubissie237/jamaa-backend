package com.jamaa.service_notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ServiceNotificationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceNotificationsApplication.class, args);
	}

}
