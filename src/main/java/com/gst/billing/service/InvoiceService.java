package com.gst.billing.service;

import com.gst.billing.model.*;
import com.gst.billing.repository.InvoiceRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceService {

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ProductService productService;

	// Create new invoice
	@Transactional
	public Invoice createInvoice(InvoiceRequest request, User user) {
		// Validate customer
		Customer customer = customerService.getCustomerById(request.getCustomerId());

		Invoice invoice = new Invoice();
		invoice.setInvoiceNumber(generateInvoiceNumber());
		invoice.setInvoiceDate(LocalDate.now());
		invoice.setCustomer(customer);
		invoice.setUser(user);

		BigDecimal subtotal = BigDecimal.ZERO;
		BigDecimal totalGst = BigDecimal.ZERO;

		// Process invoice items
		for (InvoiceItemRequest itemRequest : request.getItems()) {
			Product product = productService.getProductById(itemRequest.getProductId());

			InvoiceItem item = new InvoiceItem();
			item.setInvoice(invoice);
			item.setProduct(product);
			item.setQuantity(itemRequest.getQuantity());
			item.setUnitPrice(product.getPrice());
			item.setGstRate(product.getGstRate());
			item.calculateAmounts();

			invoice.getItems().add(item);

			subtotal = subtotal.add(item.getSubtotal());
			totalGst = totalGst.add(item.getGstAmount());
		}

		invoice.setSubtotal(subtotal);
		invoice.setTotalGst(totalGst);
		invoice.setTotalAmount(subtotal.add(totalGst));

		return invoiceRepository.save(invoice);
	}

	// Generate unique invoice number
	private String generateInvoiceNumber() {
		String timestamp = String.valueOf(System.currentTimeMillis());
		String random = String.valueOf((int) (Math.random() * 1000));
		return "INV-" + timestamp.substring(timestamp.length() - 6) + "-" + random;
	}

	// Get invoice by ID
	public Invoice getInvoiceById(Long id) {
		return invoiceRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));
	}

	// Get all invoices for a specific user
	public List<Invoice> getUserInvoices(User user) {
		return invoiceRepository.findByUserOrderByInvoiceDateDesc(user);
	}

	// Get all invoices (admin only)
	public List<Invoice> getAllInvoices() {
		return invoiceRepository.findAllByOrderByInvoiceDateDesc();
	}

	// Get invoices by date range
	public List<Invoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate, User user) {
		return invoiceRepository.findByInvoiceDateBetweenAndUserOrderByInvoiceDateDesc(startDate, endDate, user);
	}

	// Get invoices for a specific customer
	public List<Invoice> getInvoicesByCustomer(Long customerId, User user) {
		Customer customer = customerService.getCustomerById(customerId);
		return invoiceRepository.findByCustomerAndUserOrderByInvoiceDateDesc(customer, user);
	}

	// Update invoice (limited updates - mainly for corrections)
	@Transactional
	public Invoice updateInvoice(Long id, InvoiceRequest request, User user) {
		Invoice existingInvoice = getInvoiceById(id);

		// Check if user owns this invoice or is admin
		if (!existingInvoice.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
			throw new SecurityException("You are not authorized to update this invoice");
		}

		// Only allow updates to certain fields
		if (request.getCustomerId() != null) {
			Customer customer = customerService.getCustomerById(request.getCustomerId());
			existingInvoice.setCustomer(customer);
		}

		// Recalculate if items are updated
		if (request.getItems() != null && !request.getItems().isEmpty()) {
			existingInvoice.getItems().clear();

			BigDecimal subtotal = BigDecimal.ZERO;
			BigDecimal totalGst = BigDecimal.ZERO;

			for (InvoiceItemRequest itemRequest : request.getItems()) {
				Product product = productService.getProductById(itemRequest.getProductId());

				InvoiceItem item = new InvoiceItem();
				item.setInvoice(existingInvoice);
				item.setProduct(product);
				item.setQuantity(itemRequest.getQuantity());
				item.setUnitPrice(product.getPrice());
				item.setGstRate(product.getGstRate());
				item.calculateAmounts();

				existingInvoice.getItems().add(item);

				subtotal = subtotal.add(item.getSubtotal());
				totalGst = totalGst.add(item.getGstAmount());
			}

			existingInvoice.setSubtotal(subtotal);
			existingInvoice.setTotalGst(totalGst);
			existingInvoice.setTotalAmount(subtotal.add(totalGst));
		}

		return invoiceRepository.save(existingInvoice);
	}

	// Delete invoice
	@Transactional
	public void deleteInvoice(Long id, User user) {
		Invoice invoice = getInvoiceById(id);

		// Check if user owns this invoice or is admin
		if (!invoice.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
			throw new SecurityException("You are not authorized to delete this invoice");
		}

		invoiceRepository.delete(invoice);
	}

	// Get invoice statistics for user
	public InvoiceStatistics getInvoiceStatistics(User user) {
		List<Invoice> userInvoices = getUserInvoices(user);

		BigDecimal totalRevenue = userInvoices.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		BigDecimal totalGst = userInvoices.stream().map(Invoice::getTotalGst).reduce(BigDecimal.ZERO, BigDecimal::add);

		long totalInvoices = userInvoices.size();

		return new InvoiceStatistics(totalInvoices, totalRevenue, totalGst);
	}

	// Search invoices by invoice number
	public List<Invoice> searchInvoicesByNumber(String invoiceNumber, User user) {
		return invoiceRepository.findByInvoiceNumberContainingIgnoreCaseAndUser(invoiceNumber, user);
	}

	// Get invoice count for user
	public long getInvoiceCount(User user) {
		return invoiceRepository.countByUser(user);
	}

	// Get total sales amount for user
	public BigDecimal getTotalSalesByUser(User user) {
		BigDecimal total = invoiceRepository.getTotalSalesByUser(user);
		return total != null ? total : BigDecimal.ZERO;
	}

	// Get total GST collected for user
	public BigDecimal getTotalGstByUser(User user) {
		BigDecimal total = invoiceRepository.getTotalGstByUser(user);
		return total != null ? total : BigDecimal.ZERO;
	}

	public List<Invoice> getInvoicesByUserId(Long userId) {
		return invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId);
	}

	// Helper class for statistics
	public static class InvoiceStatistics {
		private final long totalInvoices;
		private final BigDecimal totalRevenue;
		private final BigDecimal totalGst;

		public InvoiceStatistics(long totalInvoices, BigDecimal totalRevenue, BigDecimal totalGst) {
			this.totalInvoices = totalInvoices;
			this.totalRevenue = totalRevenue;
			this.totalGst = totalGst;
		}

		// Getters
		public long getTotalInvoices() {
			return totalInvoices;
		}

		public BigDecimal getTotalRevenue() {
			return totalRevenue;
		}

		public BigDecimal getTotalGst() {
			return totalGst;
		}
	}

	public Map<String, BigDecimal> getMonthlyRevenue(User user, boolean isAdmin) {
		List<Object[]> data = isAdmin ? invoiceRepository.getMonthlyRevenueAll()
				: invoiceRepository.getMonthlyRevenue(user);

		Map<String, BigDecimal> result = new LinkedHashMap<>();
		for (Object[] row : data) {
			int month = ((Number) row[0]).intValue();
			BigDecimal total = (BigDecimal) row[1];
			result.put(getMonthName(month), total);
		}
		return result;
	}

	public Map<String, BigDecimal> getDailyRevenue(User user, boolean isAdmin) {
		List<Object[]> data = isAdmin ? invoiceRepository.getDailyRevenueAll()
				: invoiceRepository.getDailyRevenue(user);

		Map<String, BigDecimal> result = new LinkedHashMap<>();
		for (Object[] row : data) {
			String date = row[0].toString();
			BigDecimal total = (BigDecimal) row[1];
			result.put(date, total);
		}
		return result;
	}

	private String getMonthName(int month) {
		return java.time.Month.of(month).name().substring(0, 3); // JAN, FEB...
	}

}