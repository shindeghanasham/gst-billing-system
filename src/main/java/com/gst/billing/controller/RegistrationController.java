package com.gst.billing.controller;

import com.gst.billing.model.User;
import com.gst.billing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

	@Autowired
	private UserService userService;

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@ModelAttribute User user, Model model) {
		try {
			userService.registerUser(user);
			model.addAttribute("success", "Registration successful! Please login.");
			return "login";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("user", user);
			return "register";
		}
	}
}