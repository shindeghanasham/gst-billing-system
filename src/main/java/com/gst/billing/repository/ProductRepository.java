package com.gst.billing.repository;

import com.gst.billing.model.Product;
import com.gst.billing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	// Basic CRUD operations
	List<Product> findByCreatedByOrderByName(User createdBy);

	List<Product> findAllByOrderByName();

	Optional<Product> findByNameAndCreatedBy(String name, User createdBy);

	Optional<Product> findByHsnCodeAndCreatedBy(String hsnCode, User createdBy);

	List<Product> findByNameContainingIgnoreCaseAndCreatedBy(String name, User createdBy);

	List<Product> findByHsnCodeContainingAndCreatedBy(String hsnCode, User createdBy);

	List<Product> findByPriceBetweenAndCreatedBy(BigDecimal minPrice, BigDecimal maxPrice, User createdBy);

	List<Product> findByGstRateAndCreatedBy(BigDecimal gstRate, User createdBy);

	Boolean existsByNameAndCreatedBy(String name, User createdBy);

	Boolean existsByHsnCodeAndCreatedBy(String hsnCode, User createdBy);

	long countByCreatedBy(User createdBy);

	// Simple search query
	@Query("SELECT p FROM Product p WHERE p.createdBy = :user AND "
			+ "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(p.hsnCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
	List<Product> searchProducts(@Param("user") User user, @Param("searchTerm") String searchTerm);

	// Simple ordering queries
	@Query("SELECT p FROM Product p WHERE p.createdBy = :user ORDER BY p.price DESC")
	List<Product> findTopExpensiveProductsByUser(@Param("user") User user);

	@Query("SELECT p FROM Product p WHERE p.createdBy = :user ORDER BY p.price ASC")
	List<Product> findTopCheapestProductsByUser(@Param("user") User user);

	// Remove complex date queries for now - add them back later
}