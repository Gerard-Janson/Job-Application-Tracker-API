package com.gerard.jobapplicationtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class JobApplicationTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobApplicationTrackerApplication.class, args);
	}

}
