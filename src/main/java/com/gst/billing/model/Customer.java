package com.gst.billing.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "customers")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Customer name is required")
	@Column(nullable = false, length = 100)
	private String name;

	@Email(message = "Invalid email format")
	@Column(unique = true, length = 100)
	private String email;

	@Column(length = 15)
	private String phone;

	@Column(columnDefinition = "TEXT")
	private String address;

	@Column(length = 15)
	private String gstin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	// Constructors
	public Customer() {
		this.createdAt = LocalDateTime.now();
	}

	public Customer(String name, String email, String phone, String address, String gstin) {
		this();
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.address = address;
		this.gstin = gstin;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getGstin() {
		return gstin;
	}

	public void setGstin(String gstin) {
		this.gstin = gstin;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	@Override
	public String toString() {
		return "Customer{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + ", phone='" + phone
				+ '\'' + ", gstin='" + gstin + '\'' + '}';
	}
}