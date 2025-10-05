package com.gst.billing.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String invoiceNumber;

	@Column(name = "invoice_date", nullable = false)
	private LocalDate invoiceDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false) // Lazy loading + required
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Column(name = "total_gst", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalGst = BigDecimal.ZERO;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InvoiceItem> items = new ArrayList<>();

	// ---------- Constructors ----------
	public Invoice() {
	}

	// ---------- Utility methods ----------
	public void addItem(InvoiceItem item) {
		items.add(item);
		item.setInvoice(this); // maintain bidirectional consistency
	}

	public void removeItem(InvoiceItem item) {
		items.remove(item);
		item.setInvoice(null);
	}

	// ---------- Lifecycle ----------
	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (invoiceDate == null) {
			invoiceDate = LocalDate.now();
		}
	}

	// ---------- Getters & Setters ----------
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getTotalGst() {
		return totalGst;
	}

	public void setTotalGst(BigDecimal totalGst) {
		this.totalGst = totalGst;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<InvoiceItem> getItems() {
		return items;
	}

	public void setItems(List<InvoiceItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "Invoice{" + "id=" + id + ", invoiceNumber='" + invoiceNumber + '\'' + ", invoiceDate=" + invoiceDate
				+ ", totalAmount=" + totalAmount + '}';
	}

}
