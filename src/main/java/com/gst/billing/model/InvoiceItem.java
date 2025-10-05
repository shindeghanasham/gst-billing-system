package com.gst.billing.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private Invoice invoice;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private Integer quantity = 1;

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice = BigDecimal.ZERO;

	@Column(name = "gst_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal gstRate = BigDecimal.ZERO;

	@Column(name = "gst_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal gstAmount = BigDecimal.ZERO;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	// ---------- Constructors ----------
	public InvoiceItem() {
	}

	public InvoiceItem(Product product, Integer quantity) {
		this.product = product;
		this.quantity = quantity != null ? quantity : 1;
		if (product != null) {
			this.unitPrice = product.getPrice();
			this.gstRate = product.getGstRate();
		}
		calculateAmounts();
	}

	// ---------- Business methods ----------
	public void calculateAmounts() {
		if (unitPrice != null && quantity != null && gstRate != null) {
			BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
			this.gstAmount = itemTotal.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
			this.totalAmount = itemTotal.add(gstAmount);
		} else {
			this.gstAmount = BigDecimal.ZERO;
			this.totalAmount = BigDecimal.ZERO;
		}
	}

	// ---------- Getters & Setters ----------
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
		if (product != null) {
			this.unitPrice = product.getPrice();
			this.gstRate = product.getGstRate();
			calculateAmounts();
		}
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity != null ? quantity : 1;
		calculateAmounts();
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
		calculateAmounts();
	}

	public BigDecimal getGstRate() {
		return gstRate;
	}

	public void setGstRate(BigDecimal gstRate) {
		this.gstRate = gstRate != null ? gstRate : BigDecimal.ZERO;
		calculateAmounts();
	}

	public BigDecimal getGstAmount() {
		return gstAmount;
	}

	public void setGstAmount(BigDecimal gstAmount) {
		this.gstAmount = gstAmount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	// Helper method to get subtotal before GST
	// Add this method to InvoiceItem.java
	public BigDecimal getSubtotal() {
		if (unitPrice != null && quantity != null) {
			return unitPrice.multiply(BigDecimal.valueOf(quantity));
		}
		return BigDecimal.ZERO;
	}

	@Override
	public String toString() {
		return "InvoiceItem{" + "id=" + id + ", product=" + (product != null ? product.getName() : "null")
				+ ", quantity=" + quantity + ", unitPrice=" + unitPrice + ", totalAmount=" + totalAmount + '}';
	}
}
