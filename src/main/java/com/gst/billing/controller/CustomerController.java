package com.gst.billing.controller;

import com.gst.billing.model.Customer;
import com.gst.billing.model.User;
import com.gst.billing.service.CustomerService;
import com.gst.billing.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

	@Autowired
	private CustomerService customerService;

	@Autowired
	private UserService userService;

	@GetMapping
	public String listCustomers(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		User currentUser = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<Customer> customers = customerService.getUserCustomers(currentUser);
		model.addAttribute("customers", customers);
		model.addAttribute("title", "Manage Customers");
		return "customers/list";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("customer", new Customer());
		model.addAttribute("title", "Create Customer");
		return "customers/create";
	}

	@PostMapping("/create")
	public String createCustomer(@Valid @ModelAttribute Customer customer, BindingResult result,
			@AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("title", "Create Customer");
			return "customers/create";
		}

		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			customerService.createCustomer(customer, currentUser);
			redirectAttributes.addFlashAttribute("message", "Customer created successfully!");
			return "redirect:/customers";
		} catch (Exception e) {
			model.addAttribute("error", "Error creating customer: " + e.getMessage());
			model.addAttribute("title", "Create Customer");
			return "customers/create";
		}
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			Customer customer = customerService.getCustomerById(id);

			// Check if user owns this customer or is admin
			if (!customer.getCreatedBy().getId().equals(currentUser.getId())
					&& !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
				return "redirect:/access-denied";
			}

			model.addAttribute("customer", customer);
			model.addAttribute("title", "Edit Customer");
			return "customers/edit";
		} catch (Exception e) {
			return "redirect:/customers";
		}
	}

	@PostMapping("/edit/{id}")
	public String updateCustomer(@PathVariable Long id, @Valid @ModelAttribute Customer customer, BindingResult result,
			@AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("title", "Edit Customer");
			return "customers/edit";
		}

		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			customerService.updateCustomer(id, customer, currentUser);
			redirectAttributes.addFlashAttribute("message", "Customer updated successfully!");
			return "redirect:/customers";
		} catch (Exception e) {
			model.addAttribute("error", "Error updating customer: " + e.getMessage());
			model.addAttribute("title", "Edit Customer");
			return "customers/edit";
		}
	}

	@GetMapping("/delete/{id}")
	public String deleteCustomer(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			customerService.deleteCustomer(id, currentUser);
			redirectAttributes.addFlashAttribute("message", "Customer deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error deleting customer: " + e.getMessage());
		}
		return "redirect:/customers";
	}

	@GetMapping("/search")
	public String searchCustomers(@RequestParam String query, @AuthenticationPrincipal UserDetails userDetails,
			Model model) {
		User currentUser = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<Customer> customers = customerService.searchCustomers(query, currentUser);
		model.addAttribute("customers", customers);
		model.addAttribute("searchQuery", query);
		model.addAttribute("title", "Search Customers");
		return "customers/list";
	}
}