package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repo.RoleRepository;
import com.example.demo.repo.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService , UserDetailsService{
	
	private RoleRepository roleRepository;
	
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findOneByUserName(username).get();
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		user.getRoles().forEach(role -> {
			authorities.add(new SimpleGrantedAuthority(role.getName()));
		});
		
		if (user == null) {
			throw new UsernameNotFoundException(username);
		} else {
			return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(), authorities);
		}
		
	}
	

	@Override
	public User saveUser(User user) {
		log.info("Saving the user {} to the database", user.getName());
		return userRepository.save(user);
	}

	@Override
	public Role saveRole(Role role) {
		log.info("Saving the role {} to the database",role.getName());
		return roleRepository.save(role);
	}

	@Override
	public void roleToUser(String userName, String roleName) {
		User user = userRepository.findByUserName(userName).get(0);
		Role role = roleRepository.findByName(roleName);
		log.info("Adding the role {} to the user {}",userName,roleName);
		user.getRoles().add(role);
		
	}

	@Override
	public User getUser(String userName) {
		log.info("fetch the user by userName : {}", userName);
		return userRepository.findByUserName(userName).get(0);
	}

	@Override
	public List<User> getAllUser() {
		log.info("fetch all the user from the db");
		return userRepository.findAll();
	}

	

}
