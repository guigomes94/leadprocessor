package com.desafio.leadprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class LeadprocessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeadprocessorApplication.class, args);
	}

}
