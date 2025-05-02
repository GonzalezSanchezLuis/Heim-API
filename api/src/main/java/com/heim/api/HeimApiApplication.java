package com.heim.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.heim.api")
public class HeimApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HeimApiApplication.class, args);
	}

}
