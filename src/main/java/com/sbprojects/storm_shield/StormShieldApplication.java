package com.sbprojects.storm_shield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //Activates Spring's background automation task engine
public class StormShieldApplication {

	public static void main(String[] args) {
		SpringApplication.run(StormShieldApplication.class, args);
	}

}
