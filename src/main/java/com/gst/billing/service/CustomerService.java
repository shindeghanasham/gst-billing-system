package com.gst.billing.service;

import com.gst.billing.model.Customer;
import com.gst.billing.model.User;
import com.gst.billing.repository.CustomerRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

	@Autowired
	private CustomerRepository customerRepository;

	// Create new customer
	public Customer createCustomer(Customer customer, User createdBy) {
		customer.setCreatedBy(createdBy);
		return customerRepository.save(customer);
	}

	// Get customer by ID
	public Customer getCustomerById(Long id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
	}

	// Get all customers for a specific user
	public List<Customer> getUserCustomers(User user) {
		return customerRepository.findByCreatedByOrderByName(user);
	}

	// Get all customers (admin only)
	public List<Customer> getAllCustomers() {
		return customerRepository.findAllByOrderByName();
	}
	
	// Get recent customers (last N days)
	

	// Update customer
	public Customer updateCustomer(Long id, Customer customerDetails, User user) {
		Customer existingCustomer = getCustomerById(id);

		// Check if user owns this customer or is admin
		if (!existingCustomer.getCreatedBy().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
			throw new SecurityException("You are not authorized to update this customer");
		}

		existingCustomer.setName(customerDetails.getName());
		existingCustomer.setEmail(customerDetails.getEmail());
		existingCustomer.setPhone(customerDetails.getPhone());
		existingCustomer.setAddress(customerDetails.getAddress());
		existingCustomer.setGstin(customerDetails.getGstin());

		return customerRepository.save(existingCustomer);
	}

	// Delete customer
	public void deleteCustomer(Long id, User user) {
		Customer customer = getCustomerById(id);

		// Check if user owns this customer or is admin
		if (!customer.getCreatedBy().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
			throw new SecurityException("You are not authorized to delete this customer");
		}

		customerRepository.delete(customer);
	}

	// Search customers by name
	public List<Customer> searchCustomers(String name, User user) {
		return customerRepository.findByNameContainingIgnoreCaseAndCreatedBy(name, user);
	}

	// Check if customer exists by email
	public boolean existsByEmail(String email) {
		return customerRepository.existsByEmail(email);
	}

	// Check if customer exists by GSTIN
	public boolean existsByGstin(String gstin) {
		return customerRepository.existsByGstin(gstin);
	}

	// Get customer by email
	public Optional<Customer> getCustomerByEmail(String email) {
		return customerRepository.findByEmail(email);
	}

	// Get customer count for user
	public long getCustomerCount(User user) {
		return customerRepository.countByCreatedBy(user);
	}
}