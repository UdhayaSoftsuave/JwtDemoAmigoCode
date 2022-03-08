package com.example.demo;

import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.service.UserService;

@SpringBootApplication
public class JwtDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(JwtDemoApplication.class, args);
	}
	
	@Bean
	CommandLineRunner run(UserService userService) {
		return args ->{
			userService.saveRole(new Role(null , "ADMIN"));
			userService.saveRole(new Role(null , "USER"));
			
			userService.saveUser(new User(null, "ABC", "ABC", passwordEncoder().encode("123"), new ArrayList<>()));
			userService.saveUser(new User(null, "XYZ", "XYZ", passwordEncoder().encode("999"), new ArrayList<>()));
			
			userService.roleToUser("ABC", "ADMIN");
			userService.roleToUser("ABC", "USER");
			userService.roleToUser("XYZ", "USER");
			
		};
	}
	
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	//refer by : https://www.youtube.com/watch?v=VVn9OG9nfH0&t=54s
}
