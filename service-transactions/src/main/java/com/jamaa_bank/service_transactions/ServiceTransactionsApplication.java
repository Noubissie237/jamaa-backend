package com.jamaa_bank.service_transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ServiceTransactionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceTransactionsApplication.class, args);
	}

}
