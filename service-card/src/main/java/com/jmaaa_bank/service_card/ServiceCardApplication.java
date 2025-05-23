package com.jmaaa_bank.service_card;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ServiceCardApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceCardApplication.class, args);
	}

}
