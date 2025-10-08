package com.youthconnect.edge_functions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EdgeFunctionsApplication {
	public static void main(String[] args) {
		SpringApplication.run(EdgeFunctionsApplication.class, args);
	}
}