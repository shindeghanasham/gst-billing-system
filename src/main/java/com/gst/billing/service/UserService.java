package com.gst.billing.service;

import com.gst.billing.model.User;
import com.gst.billing.model.Role;
import com.gst.billing.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public User registerUser(User user) {
		// Check if username already exists
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			throw new RuntimeException("Username already exists");
		}

		// Check if email already exists
		if (userRepository.findByEmail(user.getEmail()).isPresent()) {
			throw new RuntimeException("Email already exists");
		}

		// Encode password and set default role
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole(Role.USER);
		user.setEnabled(true);

		return userRepository.save(user);
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public long getAdminUsersCount() {
		return userRepository.countByRole(Role.ADMIN);
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
	}

	public User updateUser(User user) {
		return userRepository.save(user);
	}

	public void deleteUser(Long id) {
		User user = getUserById(id);
		userRepository.delete(user);
	}

	public long getUserCount() {
		return userRepository.count();
	}

	public long getActiveUsersCount() {
		return userRepository.countByEnabledTrue();
	}

	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("Current user not found"));
	}

	public List<User> getUsersByRole(Role role) {
		return userRepository.findByRole(role);
	}

	public boolean toggleUserStatus(Long id) {
		User user = getUserById(id);
		user.setEnabled(!user.isEnabled());
		userRepository.save(user);
		return user.isEnabled();
	}

}