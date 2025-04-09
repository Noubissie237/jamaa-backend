package com.jamaa.service_banks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ServiceBanksApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceBanksApplication.class, args);
	}

}
