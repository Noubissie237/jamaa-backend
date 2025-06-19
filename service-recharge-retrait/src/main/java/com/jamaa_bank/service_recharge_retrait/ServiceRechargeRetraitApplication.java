package com.jamaa_bank.service_recharge_retrait;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableDiscoveryClient
@EnableRetry
@EnableTransactionManagement
@SpringBootApplication
public class ServiceRechargeRetraitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRechargeRetraitApplication.class, args);
	}

}
