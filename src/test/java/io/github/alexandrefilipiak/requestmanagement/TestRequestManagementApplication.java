package io.github.alexandrefilipiak.requestmanagement;

import org.springframework.boot.SpringApplication;

public class TestRequestManagementApplication {

	public static void main(String[] args) {
		SpringApplication.from(RequestManagementApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
