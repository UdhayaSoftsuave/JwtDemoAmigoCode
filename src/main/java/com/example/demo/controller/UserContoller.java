package com.example.demo.controller;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.demo.DTO.AddRoleToUser;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserContoller {
	
	private UserService userService;
	
	private PasswordEncoder passwordEncoder;
	
	@GetMapping("/users")
	public ResponseEntity<List<User>> getAllUser(){
		return ResponseEntity.ok().body(userService.getAllUser());
	}
	
	@PostMapping("/user/save")
	public ResponseEntity<User> saveUser(@RequestBody User user){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return ResponseEntity.created(uri).body(userService.saveUser(user));
	}
	
	@PostMapping("/role/save")
	public ResponseEntity<Role> saveRole(@RequestBody Role role){
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
		return ResponseEntity.created(uri).body(userService.saveRole(role));
	}
	
	@PostMapping("/role/addRoleToUser")
	public ResponseEntity<?> addRoleToUser(@RequestBody AddRoleToUser addRoleToUser){
		userService.roleToUser(addRoleToUser.getUserName(), addRoleToUser.getRoleName());
		return ResponseEntity.ok().build();
	}
	@GetMapping("/token/refersh")
	public void refershToken(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String authToken = request.getHeader("Authorization");
		if (authToken != null && authToken.startsWith("Bearer ")) {
			try {
				String referesh_token = authToken.substring("Bearer ".length());
				System.out.println(referesh_token);
				Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
				JWTVerifier jwtVerifier = JWT.require(algorithm).build();
				DecodedJWT decodedJWT = jwtVerifier.verify(referesh_token);
				User user = userService.getUser(decodedJWT.getSubject()); 
				
				String access_token = JWT.create()
						.withSubject(user.getUserName())
						.withExpiresAt(new Date(System.currentTimeMillis() + 10* 60 *1000))
						.withIssuer(request.getRequestURI().toString())
						.withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
						.sign(algorithm);
			
			Map<String, String> tokens = new HashMap<>();
			tokens.put("access_token", access_token);
			tokens.put("referesh_token", referesh_token);
			response.setContentType("application/json");
			new ObjectMapper().writeValue(response.getOutputStream(), tokens);
			} catch (Exception e) {
				response.setHeader("error", e.getMessage()); 
				System.out.println(e.getMessage());
				response.setStatus(403);
				response.setContentType("application/json");
				Map<String, String> res = new HashMap<>();
				res.put("error", e.getMessage());
				new ObjectMapper().writeValue(response.getOutputStream(), res);
			}
					
		}else {
			throw new RuntimeException("Referesh token is missing ");
		}
		
		
	}
	

}
