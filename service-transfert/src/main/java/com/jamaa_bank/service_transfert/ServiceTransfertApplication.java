package com.jamaa_bank.service_transfert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ServiceTransfertApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceTransfertApplication.class, args);
	}

}
