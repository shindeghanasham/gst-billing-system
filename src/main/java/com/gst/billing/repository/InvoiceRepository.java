package com.gst.billing.repository;

import com.gst.billing.model.Customer;
import com.gst.billing.model.Invoice;
import com.gst.billing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	// Basic CRUD operations
	List<Invoice> findByUserOrderByInvoiceDateDesc(User user);

	List<Invoice> findAllByOrderByInvoiceDateDesc();

	Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

	List<Invoice> findByCustomerAndUserOrderByInvoiceDateDesc(Customer customer, User user);

	List<Invoice> findByInvoiceDateBetweenAndUserOrderByInvoiceDateDesc(LocalDate startDate, LocalDate endDate,
			User user);

	List<Invoice> findByInvoiceDateAndUserOrderByInvoiceDateDesc(LocalDate invoiceDate, User user);

	List<Invoice> findByInvoiceNumberContainingIgnoreCaseAndUser(String invoiceNumber, User user);

	long countByUser(User user);

	long countByInvoiceDate(LocalDate invoiceDate);

	Boolean existsByInvoiceNumber(String invoiceNumber);

	List<Invoice> findByUserIdOrderByInvoiceDateDesc(Long userId);

	// Simple queries
	@Query("SELECT i FROM Invoice i WHERE i.user = :user ORDER BY i.invoiceDate DESC")
	List<Invoice> findLatestInvoicesByUser(@Param("user") User user);

	@Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.user = :user")
	BigDecimal getTotalSalesByUser(@Param("user") User user);

	@Query("SELECT COALESCE(SUM(i.totalGst), 0) FROM Invoice i WHERE i.user = :user")
	BigDecimal getTotalGstByUser(@Param("user") User user);

	// Simple search
	@Query("SELECT i FROM Invoice i WHERE i.user = :user AND "
			+ "(LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
			+ "LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
	List<Invoice> searchInvoices(@Param("user") User user, @Param("searchTerm") String searchTerm);

	@Query("SELECT MONTH(i.invoiceDate), SUM(i.totalAmount) FROM Invoice i WHERE i.user = :user GROUP BY MONTH(i.invoiceDate) ORDER BY MONTH(i.invoiceDate)")
	List<Object[]> getMonthlyRevenue(User user);

	// 📅 Day-wise total revenue for user
	@Query("SELECT DATE(i.invoiceDate), SUM(i.totalAmount) FROM Invoice i WHERE i.user = :user GROUP BY DATE(i.invoiceDate) ORDER BY DATE(i.invoiceDate)")
	List<Object[]> getDailyRevenue(User user);

	// 🔹 Admin — all users together
	@Query("SELECT MONTH(i.invoiceDate), SUM(i.totalAmount) FROM Invoice i GROUP BY MONTH(i.invoiceDate) ORDER BY MONTH(i.invoiceDate)")
	List<Object[]> getMonthlyRevenueAll();

	@Query("SELECT DATE(i.invoiceDate), SUM(i.totalAmount) FROM Invoice i GROUP BY DATE(i.invoiceDate) ORDER BY DATE(i.invoiceDate)")
	List<Object[]> getDailyRevenueAll();

}