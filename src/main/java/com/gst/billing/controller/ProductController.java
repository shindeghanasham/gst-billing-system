package com.gst.billing.controller;

import com.gst.billing.model.Product;
import com.gst.billing.model.User;
import com.gst.billing.service.ProductService;
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
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@GetMapping
	public String listProducts(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		User currentUser = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<Product> products = productService.getUserProducts(currentUser);
		model.addAttribute("products", products);
		model.addAttribute("title", "Manage Products");
		return "products/list";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("title", "Create Product");
		return "products/create";
	}

	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute Product product, BindingResult result,
			@AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("title", "Create Product");
			return "products/create";
		}

		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			productService.createProduct(product, currentUser);
			redirectAttributes.addFlashAttribute("message", "Product created successfully!");
			return "redirect:/products";
		} catch (Exception e) {
			model.addAttribute("error", "Error creating product: " + e.getMessage());
			model.addAttribute("title", "Create Product");
			return "products/create";
		}
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			Product product = productService.getProductById(id);

			// Check if user owns this product or is admin
			if (!product.getCreatedBy().getId().equals(currentUser.getId())
					&& !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
				return "redirect:/access-denied";
			}

			model.addAttribute("product", product);
			model.addAttribute("title", "Edit Product");
			return "products/edit";
		} catch (Exception e) {
			return "redirect:/products";
		}
	}

	@PostMapping("/edit/{id}")
	public String updateProduct(@PathVariable Long id, @Valid @ModelAttribute Product product, BindingResult result,
			@AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("title", "Edit Product");
			return "products/edit";
		}

		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			productService.updateProduct(id, product, currentUser);
			redirectAttributes.addFlashAttribute("message", "Product updated successfully!");
			return "redirect:/products";
		} catch (Exception e) {
			model.addAttribute("error", "Error updating product: " + e.getMessage());
			model.addAttribute("title", "Edit Product");
			return "products/edit";
		}
	}

	@GetMapping("/delete/{id}")
	public String deleteProduct(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));

			productService.deleteProduct(id, currentUser);
			redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error deleting product: " + e.getMessage());
		}
		return "redirect:/products";
	}

	@GetMapping("/search")
	public String searchProducts(@RequestParam String query, @AuthenticationPrincipal UserDetails userDetails,
			Model model) {
		User currentUser = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<Product> products = productService.searchProducts(query, currentUser);
		model.addAttribute("products", products);
		model.addAttribute("searchQuery", query);
		model.addAttribute("title", "Search Products");
		return "products/list";
	}

	@GetMapping("/search-by-hsn")
	public String searchProductsByHsn(@RequestParam String hsnCode, @AuthenticationPrincipal UserDetails userDetails,
			Model model) {
		User currentUser = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<Product> products = productService.searchProductsByHsnCode(hsnCode, currentUser);
		model.addAttribute("products", products);
		model.addAttribute("searchQuery", hsnCode);
		model.addAttribute("title", "Search Products by HSN");
		return "products/list";
	}
}