package com.gst.billing.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Product name is required")
	@Column(nullable = false, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@NotNull(message = "Price is required")
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "hsn_code", length = 10)
	private String hsnCode;

	@NotNull(message = "GST rate is required")
	@Column(name = "gst_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal gstRate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	// Constructors
	public Product() {
		this.createdAt = LocalDateTime.now();
	}

	public Product(String name, String description, BigDecimal price, String hsnCode, BigDecimal gstRate) {
		this();
		this.name = name;
		this.description = description;
		this.price = price;
		this.hsnCode = hsnCode;
		this.gstRate = gstRate;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getHsnCode() {
		return hsnCode;
	}

	public void setHsnCode(String hsnCode) {
		this.hsnCode = hsnCode;
	}

	public BigDecimal getGstRate() {
		return gstRate;
	}

	public void setGstRate(BigDecimal gstRate) {
		this.gstRate = gstRate;
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
		return "Product{" + "id=" + id + ", name='" + name + '\'' + ", price=" + price + ", gstRate=" + gstRate + '}';
	}
}