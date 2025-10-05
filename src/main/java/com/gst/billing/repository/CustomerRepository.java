package com.gst.billing.repository;

import com.gst.billing.model.Customer;
import com.gst.billing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

	// Basic CRUD operations
	List<Customer> findByCreatedByOrderByName(User createdBy);

	List<Customer> findAllByOrderByName();

	Optional<Customer> findByEmail(String email);

	List<Customer> findByNameContainingIgnoreCaseAndCreatedBy(String name, User createdBy);

	Optional<Customer> findByGstinAndCreatedBy(String gstin, User createdBy);

	Optional<Customer> findByPhoneAndCreatedBy(String phone, User createdBy);

	Boolean existsByEmail(String email);

	Boolean existsByGstin(String gstin);

	Boolean existsByEmailAndCreatedBy(String email, User createdBy);

	long countByCreatedBy(User createdBy);

	// Simple search query
	@Query("SELECT c FROM Customer c WHERE c.createdBy = :user AND "
			+ "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(c.gstin) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
	List<Customer> searchCustomers(@Param("user") User user, @Param("searchTerm") String searchTerm);

	// Remove complex date queries for now
}