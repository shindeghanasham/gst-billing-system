package com.gst.billing.controller;

import com.gst.billing.model.User;
import com.gst.billing.repository.InvoiceRepository;
import com.gst.billing.model.Role;
import com.gst.billing.model.Invoice;
import com.gst.billing.model.Customer;
import com.gst.billing.model.Product;
import com.gst.billing.service.UserService;
import com.gst.billing.service.InvoiceService;
import com.gst.billing.service.CustomerService;
import com.gst.billing.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserService userService;

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ProductService productService;

	@Autowired
	private InvoiceRepository invoiceRepository;

	// Admin Dashboard
	@GetMapping("/dashboard")
	public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		try {
			// Get all statistics for admin dashboard
			long totalUsers = userService.getUserCount();
			long totalInvoices = invoiceService.getAllInvoices().size();
			long totalCustomers = customerService.getAllCustomers().size();
			long totalProducts = productService.getAllProducts().size();

			// Calculate total revenue
			BigDecimal totalRevenue = BigDecimal.ZERO;
			List<Invoice> allInvoices = invoiceService.getAllInvoices();
			for (Invoice invoice : allInvoices) {
				totalRevenue = totalRevenue.add(invoice.getTotalAmount());
			}

			// Get recent activities
			List<Invoice> recentInvoices = allInvoices.size() > 5
					? allInvoices.subList(0, Math.min(5, allInvoices.size()))
					: allInvoices;

			model.addAttribute("totalUsers", totalUsers);
			model.addAttribute("totalInvoices", totalInvoices);
			model.addAttribute("totalCustomers", totalCustomers);
			model.addAttribute("totalProducts", totalProducts);
			model.addAttribute("totalRevenue", totalRevenue);
			model.addAttribute("recentInvoices", recentInvoices);
			model.addAttribute("title", "Admin Dashboard");

			// Monthly revenue for chart
			List<Object[]> monthlyRevenueData = invoiceRepository.getMonthlyRevenueAll();
			List<String> monthLabels = new ArrayList<>();
			List<BigDecimal> revenueValues = new ArrayList<>();

			for (Object[] obj : monthlyRevenueData) {
				Integer month = (Integer) obj[0];
				BigDecimal revenue = (BigDecimal) obj[1];
				monthLabels.add(Month.of(month).name()); // JANUARY, FEBRUARY...
				revenueValues.add(revenue);
			}

			model.addAttribute("monthLabels", monthLabels);
			model.addAttribute("revenueValues", revenueValues);

			return "admin/dashboard";
		} catch (Exception e) {
			model.addAttribute("error", "Error loading admin dashboard: " + e.getMessage());
			return "admin/dashboard";
		}
	}

	// User Management
	@GetMapping("/users")
	public String manageUsers(Model model) {
		try {
			List<User> users = userService.getAllUsers();
			model.addAttribute("users", users);
			model.addAttribute("title", "Manage Users");
			return "admin/users";
		} catch (Exception e) {
			model.addAttribute("error", "Error loading users: " + e.getMessage());
			return "admin/users";
		}
	}

	@GetMapping("/users/edit/{id}")
	public String editUserForm(@PathVariable Long id, Model model) {
		try {
			User user = userService.getUserById(id);
			model.addAttribute("user", user);
			model.addAttribute("roles", Role.values());
			model.addAttribute("title", "Edit User");
			return "admin/edit-user";
		} catch (Exception e) {
			return "redirect:/admin/users?error=User not found";
		}
	}

	@PostMapping("/users/edit/{id}")
	public String updateUser(@PathVariable Long id, @ModelAttribute User userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			User existingUser = userService.getUserById(id);
			existingUser.setRole(userDetails.getRole());
			existingUser.setEnabled(userDetails.isEnabled());

			userService.updateUser(existingUser);
			redirectAttributes.addFlashAttribute("message", "User updated successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
		}
		return "redirect:/admin/users";
	}

	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			// Prevent admin from deleting themselves
			User currentUser = userService.getCurrentUser();
			if (currentUser.getId().equals(id)) {
				redirectAttributes.addFlashAttribute("error", "You cannot delete your own account!");
				return "redirect:/admin/users";
			}

			userService.deleteUser(id);
			redirectAttributes.addFlashAttribute("message", "User deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
		}
		return "redirect:/admin/users";
	}

	// All Invoices View
	@GetMapping("/invoices")
	public String viewAllInvoices(Model model) {
		try {
			List<Invoice> invoices = invoiceService.getAllInvoices();
			BigDecimal totalRevenue = invoices.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			BigDecimal totalGst = invoices.stream().map(Invoice::getTotalGst).reduce(BigDecimal.ZERO, BigDecimal::add);

			model.addAttribute("invoices", invoices);
			model.addAttribute("totalRevenue", totalRevenue);
			model.addAttribute("totalGst", totalGst);
			model.addAttribute("title", "All Invoices");
			return "admin/invoices";
		} catch (Exception e) {
			model.addAttribute("error", "Error loading invoices: " + e.getMessage());
			return "admin/invoices";
		}
	}

	// All Customers View
	@GetMapping("/customers")
	public String viewAllCustomers(Model model) {
		try {
			List<Customer> customers = customerService.getAllCustomers();
			model.addAttribute("customers", customers);
			model.addAttribute("title", "All Customers");

			// Calculate statistics in controller
			long customersWithGstin = customers.stream()
					.filter(c -> c.getGstin() != null && !c.getGstin().trim().isEmpty()).count();

			long customersWithEmail = customers.stream()
					.filter(c -> c.getEmail() != null && !c.getEmail().trim().isEmpty()).count();

			long uniqueUsers = customers.stream().map(c -> c.getCreatedBy().getId()).distinct().count();

			// Calculate user distribution
			Map<String, UserDistribution> userDistributionMap = new HashMap<>();
			for (Customer customer : customers) {
				String username = customer.getCreatedBy().getUsername();
				String role = customer.getCreatedBy().getRole().name();

				userDistributionMap.computeIfAbsent(username, k -> new UserDistribution(username, role, 0))
						.incrementCount();
			}

			List<UserDistribution> userDistribution = new ArrayList<>(userDistributionMap.values());
			userDistribution.sort((a, b) -> Integer.compare(b.getCustomerCount(), a.getCustomerCount()));

			model.addAttribute("customersWithGstin", customersWithGstin);
			model.addAttribute("customersWithEmail", customersWithEmail);
			model.addAttribute("uniqueUsers", uniqueUsers);
			model.addAttribute("userDistribution", userDistribution);

		} catch (Exception e) {
			model.addAttribute("error", "Error loading customers: " + e.getMessage());
		}
		return "admin/customers";
	}

	// Helper class for user distribution
	public static class UserDistribution {
		private String username;
		private String role;
		private int customerCount;

		public UserDistribution(String username, String role, int customerCount) {
			this.username = username;
			this.role = role;
			this.customerCount = customerCount;
		}

		public void incrementCount() {
			this.customerCount++;
		}

		// Getters
		public String getUsername() {
			return username;
		}

		public String getRole() {
			return role;
		}

		public int getCustomerCount() {
			return customerCount;
		}
	}

	// All Products View
	@GetMapping("/products")
	public String viewAllProducts(Model model) {
		try {
			List<Product> products = productService.getAllProducts();
			model.addAttribute("products", products);
			model.addAttribute("title", "All Products");

			// Calculate statistics in controller
			long productsWithHsn = products.stream()
					.filter(p -> p.getHsnCode() != null && !p.getHsnCode().trim().isEmpty()).count();

			// Get unique GST rates
			Set<BigDecimal> uniqueGstRates = products.stream().map(Product::getGstRate).collect(Collectors.toSet());

			// Get unique users
			long uniqueUsers = products.stream().map(p -> p.getCreatedBy().getId()).distinct().count();

			// Get GST rates for template
			List<BigDecimal> gstRates = new ArrayList<>(uniqueGstRates);
			Collections.sort(gstRates);

			model.addAttribute("productsWithHsn", productsWithHsn);
			model.addAttribute("uniqueGstRates", uniqueGstRates.size());
			model.addAttribute("uniqueUsers", uniqueUsers);
			model.addAttribute("gstRates", gstRates);

		} catch (Exception e) {
			model.addAttribute("error", "Error loading products: " + e.getMessage());
		}
		return "admin/products";
	}

	// System Statistics
	@GetMapping("/statistics")
	public String systemStatistics(Model model) {
		try {
			// User statistics
			long totalUsers = userService.getUserCount();
			long activeUsers = userService.getActiveUsersCount();
			long adminUsers = userService.getAdminUsersCount();

			// Invoice statistics
			List<Invoice> allInvoices = invoiceService.getAllInvoices();
			long totalInvoices = allInvoices.size();
			BigDecimal totalRevenue = allInvoices.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO,
					BigDecimal::add);
			BigDecimal totalGst = allInvoices.stream().map(Invoice::getTotalGst).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			// Customer statistics
			long totalCustomers = customerService.getAllCustomers().size();

			// Product statistics
			long totalProducts = productService.getAllProducts().size();

			model.addAttribute("totalUsers", totalUsers);
			model.addAttribute("activeUsers", activeUsers);
			model.addAttribute("adminUsers", adminUsers);
			model.addAttribute("totalInvoices", totalInvoices);
			model.addAttribute("totalRevenue", totalRevenue);
			model.addAttribute("totalGst", totalGst);
			model.addAttribute("totalCustomers", totalCustomers);
			model.addAttribute("totalProducts", totalProducts);
			model.addAttribute("title", "System Statistics");

			return "admin/statistics";
		} catch (Exception e) {
			model.addAttribute("error", "Error loading statistics: " + e.getMessage());
			return "admin/statistics";
		}
	}
}