package com.gst.billing.controller;

import com.gst.billing.model.*;
import com.gst.billing.service.InvoiceService;
import com.gst.billing.service.ProductService;
import com.gst.billing.service.CustomerService;
import com.gst.billing.service.UserService;
import com.gst.billing.util.PdfGenerator;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private ProductService productService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private UserService userService;

	@Autowired
	private PdfGenerator pdfGenerator;

	@GetMapping("/create")
	public String showInvoiceForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			InvoiceRequest invoiceRequest = new InvoiceRequest();
			List<Product> products = productService.getUserProducts(currentUser);
			List<Customer> customers = customerService.getUserCustomers(currentUser);

			model.addAttribute("invoiceRequest", invoiceRequest);
			model.addAttribute("products", products);
			model.addAttribute("customers", customers);
			model.addAttribute("title", "Create Invoice");

			return "invoices/create";
		} catch (Exception e) {
			model.addAttribute("error", "Error loading form: " + e.getMessage());
			return "redirect:/dashboard";
		}
	}

	@PostMapping("/create")
	public String createInvoice(@Valid @ModelAttribute InvoiceRequest invoiceRequest, BindingResult result, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		// Check for validation errors
		if (result.hasErrors()) {
			try {
				User currentUser = userService.findByUsername(userDetails.getUsername())
						.orElseThrow(() -> new RuntimeException("User not found"));

				List<Product> products = productService.getUserProducts(currentUser);
				List<Customer> customers = customerService.getUserCustomers(currentUser);

				model.addAttribute("products", products);
				model.addAttribute("customers", customers);
				model.addAttribute("title", "Create Invoice");
				return "invoices/create";
			} catch (Exception e) {
				model.addAttribute("error", "Error: " + e.getMessage());
				return "invoices/create";
			}
		}

		// Check if items are present
		if (invoiceRequest.getItems() == null || invoiceRequest.getItems().isEmpty()) {
			try {
				User currentUser = userService.findByUsername(userDetails.getUsername())
						.orElseThrow(() -> new RuntimeException("User not found"));

				List<Product> products = productService.getUserProducts(currentUser);
				List<Customer> customers = customerService.getUserCustomers(currentUser);

				model.addAttribute("products", products);
				model.addAttribute("customers", customers);
				model.addAttribute("error", "Please add at least one item to the invoice");
				model.addAttribute("title", "Create Invoice");
				return "invoices/create";
			} catch (Exception e) {
				model.addAttribute("error", "Error: " + e.getMessage());
				return "invoices/create";
			}
		}

		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			// Create the invoice
			Invoice invoice = invoiceService.createInvoice(invoiceRequest, currentUser);
			return "redirect:/invoices/list?message=Invoice created successfully!";
		} catch (Exception e) {
			try {
				User currentUser = userService.findByUsername(userDetails.getUsername())
						.orElseThrow(() -> new RuntimeException("User not found"));

				List<Product> products = productService.getUserProducts(currentUser);
				List<Customer> customers = customerService.getUserCustomers(currentUser);

				model.addAttribute("products", products);
				model.addAttribute("customers", customers);
				model.addAttribute("error", "Error creating invoice: " + e.getMessage());
				model.addAttribute("title", "Create Invoice");
				return "invoices/create";
			} catch (Exception ex) {
				model.addAttribute("error", "Error: " + ex.getMessage());
				return "invoices/create";
			}
		}
	}

	@GetMapping("/list")
	public String listInvoices(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			List<Invoice> invoices = invoiceService.getUserInvoices(currentUser);

			// Calculate statistics
			long totalInvoices = invoiceService.getInvoiceCount(currentUser);
			BigDecimal totalRevenue = invoiceService.getTotalSalesByUser(currentUser);
			BigDecimal totalGst = invoiceService.getTotalGstByUser(currentUser);

			model.addAttribute("invoices", invoices);
			model.addAttribute("totalInvoices", totalInvoices);
			model.addAttribute("totalRevenue", totalRevenue);
			model.addAttribute("totalGst", totalGst);
			model.addAttribute("monthlyInvoices", invoices.size());
			model.addAttribute("title", "Manage Invoices");

			return "invoices/list";
		} catch (Exception e) {
			model.addAttribute("error", "Error loading invoices: " + e.getMessage());
			model.addAttribute("invoices", java.util.Collections.emptyList());
			return "invoices/list";
		}
	}

	@GetMapping("/search")
	public String searchInvoices(@RequestParam String query, @AuthenticationPrincipal UserDetails userDetails,
			Model model) {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			List<Invoice> invoices = invoiceService.searchInvoicesByNumber(query, currentUser);

			// Calculate statistics
			long totalInvoices = invoiceService.getInvoiceCount(currentUser);
			BigDecimal totalRevenue = invoiceService.getTotalSalesByUser(currentUser);
			BigDecimal totalGst = invoiceService.getTotalGstByUser(currentUser);

			model.addAttribute("invoices", invoices);
			model.addAttribute("totalInvoices", totalInvoices);
			model.addAttribute("totalRevenue", totalRevenue);
			model.addAttribute("totalGst", totalGst);
			model.addAttribute("monthlyInvoices", invoices.size());
			model.addAttribute("searchQuery", query);
			model.addAttribute("title", "Search Invoices");

		} catch (Exception e) {
			model.addAttribute("error", "Error searching invoices: " + e.getMessage());
			model.addAttribute("invoices", java.util.Collections.emptyList());
		}

		return "invoices/list";
	}

	@GetMapping("/view/{id}")
	public String viewInvoice(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			Invoice invoice = invoiceService.getInvoiceById(id);

			// Check if user owns this invoice or is admin
			if (!invoice.getUser().getId().equals(currentUser.getId())
					&& !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
				return "redirect:/access-denied";
			}

			// Calculate GST breakdown
			Map<BigDecimal, GstBreakdown> gstBreakdown = calculateGstBreakdown(invoice);
			model.addAttribute("gstBreakdown", gstBreakdown);
			model.addAttribute("invoice", invoice);
			model.addAttribute("title", "View Invoice - " + invoice.getInvoiceNumber());
			return "invoices/view";
		} catch (Exception e) {
			return "redirect:/invoices/list?error=Invoice not found";
		}
	}

	private Map<BigDecimal, GstBreakdown> calculateGstBreakdown(Invoice invoice) {
		Map<BigDecimal, GstBreakdown> breakdown = new HashMap<>();

		for (InvoiceItem item : invoice.getItems()) {
			BigDecimal gstRate = item.getGstRate();
			BigDecimal taxableAmount = item.getSubtotal();
			BigDecimal gstAmount = item.getGstAmount();

			breakdown.computeIfAbsent(gstRate, k -> new GstBreakdown()).addItem(taxableAmount, gstAmount);
		}

		return breakdown;
	}

	// Helper class for GST breakdown
	public static class GstBreakdown {
		private BigDecimal taxableAmount = BigDecimal.ZERO;
		private BigDecimal gstAmount = BigDecimal.ZERO;

		public void addItem(BigDecimal taxable, BigDecimal gst) {
			this.taxableAmount = this.taxableAmount.add(taxable);
			this.gstAmount = this.gstAmount.add(gst);
		}

		// Getters
		public BigDecimal getTaxableAmount() {
			return taxableAmount;
		}

		public BigDecimal getGstAmount() {
			return gstAmount;
		}

		public BigDecimal getCgst() {
			return gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
		}

		public BigDecimal getSgst() {
			return gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
		}
	}

	@GetMapping("/download/{id}")
	public void downloadInvoice(@PathVariable Long id, HttpServletResponse response,
			@AuthenticationPrincipal UserDetails userDetails) throws IOException {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			Invoice invoice = invoiceService.getInvoiceById(id);

			// Check if user owns this invoice or is admin
			if (!invoice.getUser().getId().equals(currentUser.getId())
					&& !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
				return;
			}

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition",
					"attachment; filename=invoice_" + invoice.getInvoiceNumber() + ".pdf");

			pdfGenerator.generateInvoice(invoice, response.getOutputStream());

		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating PDF: " + e.getMessage());
		}
	}

	@GetMapping("/download/excel")
	public void downloadInvoicesExcel(HttpServletResponse response, @AuthenticationPrincipal UserDetails userDetails)
			throws IOException {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=invoices.xlsx");

		List<Invoice> invoices;

		// ✅ Find current user
		User currentUser = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// ✅ If Admin → get all invoices; else → only user’s invoices
		if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			invoices = invoiceService.getAllInvoices();
		} else {
			invoices = invoiceService.getInvoicesByUserId(currentUser.getId());
		}

		// ✅ Generate Excel file
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Invoices");
		int rowNum = 0;

		// Header row
		Row headerRow = sheet.createRow(rowNum++);
		String[] headers = { "Invoice ID", "Customer Name", "Invoice Date", "Subtotal", "GST", "Total Amount" };
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
		}

		// Data rows
		for (Invoice invoice : invoices) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(invoice.getId());
			row.createCell(1).setCellValue(invoice.getCustomer().getName());
			row.createCell(2).setCellValue(invoice.getInvoiceDate().toString());
			row.createCell(3).setCellValue(invoice.getSubtotal().doubleValue());
			row.createCell(4).setCellValue(invoice.getTotalGst().doubleValue());
			row.createCell(5).setCellValue(invoice.getTotalAmount().doubleValue());
		}

		// Auto-size columns
		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}

		// Write workbook to response
		workbook.write(response.getOutputStream());
		workbook.close();
	}

	@GetMapping("/delete/{id}")
	public String deleteInvoice(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			invoiceService.deleteInvoice(id, currentUser);
			return "redirect:/invoices/list?message=Invoice deleted successfully";
		} catch (Exception e) {
			return "redirect:/invoices/list?error=Error deleting invoice: " + e.getMessage();
		}
	}
}