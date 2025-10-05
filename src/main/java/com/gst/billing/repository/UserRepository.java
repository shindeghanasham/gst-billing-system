package com.gst.billing.repository;

import com.gst.billing.model.Role;
import com.gst.billing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

	List<User> findByRole(String role);

	List<User> findByEnabledTrue();

	@Query("SELECT u FROM User u WHERE u.enabled = true AND u.role = 'USER'")
	List<User> findAllActiveUsers();

	@Query("SELECT COUNT(u) FROM User u WHERE u.role = 'USER'")
	long countAllUsers();

	@Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'")
	long countAllAdmins();

	// Add to UserRepository.java
	long countByEnabledTrue();

	long countByRole(Role role);

	List<User> findByRole(Role role);
}