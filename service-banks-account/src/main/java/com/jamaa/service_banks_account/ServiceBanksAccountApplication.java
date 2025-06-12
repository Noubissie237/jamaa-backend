package com.jamaa.service_banks_account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication
public class ServiceBanksAccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceBanksAccountApplication.class, args);
	}

}
