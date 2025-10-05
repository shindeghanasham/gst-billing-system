package com.gst.billing.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gst.billing.model.User;
import com.gst.billing.service.CustomerService;
import com.gst.billing.service.DashboardService;
import com.gst.billing.service.InvoiceService;
import com.gst.billing.service.ProductService;
import com.gst.billing.service.UserService;

@Controller
public class LoginController {
	@Autowired
	private InvoiceService invoiceService;
	@Autowired
	private UserService userService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private ProductService productService;
	@Autowired
	private DashboardService dashboardService;

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User currentUser = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		// Calculate statistics
		long totalInvoices = invoiceService.getInvoiceCount(currentUser);
		long totalCustomer = customerService.getCustomerCount(currentUser);

		long productCount = productService.getProductCount(currentUser);
		model.addAttribute("invoiceCount", totalInvoices);
		model.addAttribute("customerCount", totalCustomer);
		model.addAttribute("productCount", productCount);

		// chart data
		model.addAttribute("monthLabels", dashboardService.getMonthLabels(currentUser, isAdmin));
		model.addAttribute("monthRevenue", dashboardService.getMonthRevenue(currentUser, isAdmin));
		model.addAttribute("dayLabels", dashboardService.getDayLabels(currentUser, isAdmin));
		model.addAttribute("dayRevenue", dashboardService.getDayRevenue(currentUser, isAdmin));

		model.addAttribute("isAdmin", isAdmin);
		model.addAttribute("title", "Dashboard");

		return "dashboard";
	}

	@GetMapping("/access-denied")
	public String accessDenied() {
		return "access-denied";
	}
}