package com.gst.billing.repository;

import com.gst.billing.model.Invoice;
import com.gst.billing.model.InvoiceItem;
import com.gst.billing.model.Product;
import com.gst.billing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

	// Basic CRUD operations
	List<InvoiceItem> findByInvoice(Invoice invoice);

	List<InvoiceItem> findByProduct(Product product);

	List<InvoiceItem> findByInvoiceAndProduct(Invoice invoice, Product product);

	long countByInvoice(Invoice invoice);

	void deleteByInvoice(Invoice invoice);

	Boolean existsByProduct(Product product);

	// Simple queries
	@Query("SELECT COALESCE(SUM(ii.quantity), 0) FROM InvoiceItem ii WHERE ii.product = :product")
	Long getTotalQuantitySoldByProduct(@Param("product") Product product);

	@Query("SELECT COALESCE(SUM(ii.totalAmount), 0) FROM InvoiceItem ii WHERE ii.product = :product")
	BigDecimal getTotalRevenueByProduct(@Param("product") Product product);

	@Query("SELECT COALESCE(AVG(ii.gstRate), 0) FROM InvoiceItem ii WHERE ii.invoice.user = :user")
	Double getAverageGstRateByUser(@Param("user") User user);
}