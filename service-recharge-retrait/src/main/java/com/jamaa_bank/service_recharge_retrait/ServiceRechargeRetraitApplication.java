package com.jamaa_bank.service_recharge_retrait;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ServiceRechargeRetraitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRechargeRetraitApplication.class, args);
	}

}
