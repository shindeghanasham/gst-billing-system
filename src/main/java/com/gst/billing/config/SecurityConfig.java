package com.gst.billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// Disable CSRF for development
				.csrf(csrf -> csrf.disable())

				// Configure authorization
				.authorizeHttpRequests(authz -> authz
						// Public endpoints
						.requestMatchers("/", "/home", "/index.html", "/login", "/register", "/css/**", "/js/**",
								"/webjars/**", "/images/**", "/error", "/favicon.ico")
						.permitAll()

						// Admin only endpoints
						.requestMatchers("/admin/**").hasRole("ADMIN")

						// Authenticated user endpoints
						.requestMatchers("/dashboard", "/invoices/**", "/customers/**", "/products/**").authenticated()

						// Any other request requires authentication
						.anyRequest().authenticated())

				// Configure form login
				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
						.successHandler(customAuthenticationSuccessHandler()) // ✅ येथे Role-based redirect
						.failureUrl("/login?error=true").usernameParameter("username").passwordParameter("password")
						.permitAll())

				// Configure logout
				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true")
						.invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll())

				// Configure exception handling
				.exceptionHandling(exceptions -> exceptions.accessDeniedPage("/access-denied"))

				// Session management
				.sessionManagement(session -> session.maximumSessions(1).expiredUrl("/login?expired=true"));

		return http.build();
	}

	// ✅ Role-based Redirect Logic
	@Bean
	public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
		return (request, response, authentication) -> {
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

			if (isAdmin) {
				response.sendRedirect("/admin/dashboard"); // 🔥 Admin user redirect
			} else {
				response.sendRedirect("/dashboard"); // 🔥 Normal user redirect
			}
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
