package com.example.demo.config;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NoArgsConstructor;

public class CustomAuthorizationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if ((request.getRequestURI().equals("/login")) || (request.getRequestURI().equals("/api/token/refersh"))){
			filterChain.doFilter(request, response);
		} else {
			String authToken = request.getHeader("Authorization");
			if (authToken != null && authToken.startsWith("Bearer ")) {
				try {
					String token = authToken.substring("Bearer ".length());
					Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
					JWTVerifier jwtVerifier = JWT.require(algorithm).build();
					DecodedJWT decodedJWT = jwtVerifier.verify(token);
					String username = decodedJWT.getSubject();
					String[] role = decodedJWT.getClaim("roles").asArray(String.class);
					List<GrantedAuthority> authorities = new ArrayList<>();
					for (String grantedAuthority : role) {
						authorities.add(new SimpleGrantedAuthority(grantedAuthority));
					}
					UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
					
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
					
					filterChain.doFilter(request, response);
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
				filterChain.doFilter(request, response);
			}

		}
		
	}

}
