package com.gst.billing.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public class InvoiceRequest {

	@NotNull(message = "Customer is required")
	private Long customerId;

	private List<InvoiceItemRequest> items = new ArrayList<>();

	// Getters and Setters
	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public List<InvoiceItemRequest> getItems() {
		return items;
	}

	public void setItems(List<InvoiceItemRequest> items) {
		this.items = items;
	}
}