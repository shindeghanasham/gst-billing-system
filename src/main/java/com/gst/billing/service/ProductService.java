package com.gst.billing.service;

import com.gst.billing.model.Product;
import com.gst.billing.model.User;
import com.gst.billing.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	// Create new product
	public Product createProduct(Product product, User createdBy) {
		product.setCreatedBy(createdBy);
		return productRepository.save(product);
	}

	// Get product by ID
	public Product getProductById(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
	}

	// Get recent products (last N days)

	// Get all products for a specific user
	public List<Product> getUserProducts(User user) {
		return productRepository.findByCreatedByOrderByName(user);
	}

	// Get all products (admin only)
	public List<Product> getAllProducts() {
		return productRepository.findAllByOrderByName();
	}

	// Update product
	public Product updateProduct(Long id, Product productDetails, User user) {
		Product existingProduct = getProductById(id);

		// Check if user owns this product or is admin
		if (!existingProduct.getCreatedBy().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
			throw new SecurityException("You are not authorized to update this product");
		}

		existingProduct.setName(productDetails.getName());
		existingProduct.setDescription(productDetails.getDescription());
		existingProduct.setPrice(productDetails.getPrice());
		existingProduct.setHsnCode(productDetails.getHsnCode());
		existingProduct.setGstRate(productDetails.getGstRate());

		return productRepository.save(existingProduct);
	}

	// Delete product
	public void deleteProduct(Long id, User user) {
		Product product = getProductById(id);

		// Check if user owns this product or is admin
		if (!product.getCreatedBy().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
			throw new SecurityException("You are not authorized to delete this product");
		}

		productRepository.delete(product);
	}

	// Search products by name
	public List<Product> searchProducts(String name, User user) {
		return productRepository.findByNameContainingIgnoreCaseAndCreatedBy(name, user);
	}

	// Search products by HSN code
	public List<Product> searchProductsByHsnCode(String hsnCode, User user) {
		return productRepository.findByHsnCodeContainingAndCreatedBy(hsnCode, user);
	}

	// Get products by price range
	public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, User user) {
		return productRepository.findByPriceBetweenAndCreatedBy(minPrice, maxPrice, user);
	}

	// Get products by GST rate
	public List<Product> getProductsByGstRate(BigDecimal gstRate, User user) {
		return productRepository.findByGstRateAndCreatedBy(gstRate, user);
	}

	// Check if product exists by name for user
	public boolean existsByNameAndUser(String name, User user) {
		return productRepository.existsByNameAndCreatedBy(name, user);
	}

	// Get product by HSN code
	public Optional<Product> getProductByHsnCode(String hsnCode, User user) {
		return productRepository.findByHsnCodeAndCreatedBy(hsnCode, user);
	}

	// Get product count for user
	public long getProductCount(User user) {
		return productRepository.countByCreatedBy(user);
	}

	// Bulk product creation
	public List<Product> createProducts(List<Product> products, User createdBy) {
		products.forEach(product -> product.setCreatedBy(createdBy));
		return productRepository.saveAll(products);
	}
}