package com.gst.billing.repository;

import com.gst.billing.model.Invoice;
import com.gst.billing.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InvoiceRepositoryCustomImpl implements InvoiceRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Invoice> findInvoicesWithComplexCriteria(User user, Map<String, Object> criteria) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Invoice> query = cb.createQuery(Invoice.class);
		Root<Invoice> invoice = query.from(Invoice.class);

		List<Predicate> predicates = new ArrayList<>();

		// Always filter by user
		predicates.add(cb.equal(invoice.get("user"), user));

		// Add dynamic criteria
		if (criteria.containsKey("customerName")) {
			predicates.add(cb.like(cb.lower(invoice.get("customer").get("name")),
					"%" + criteria.get("customerName").toString().toLowerCase() + "%"));
		}

		if (criteria.containsKey("minAmount")) {
			predicates.add(cb.greaterThanOrEqualTo(invoice.get("totalAmount"), (Double) criteria.get("minAmount")));
		}

		if (criteria.containsKey("maxAmount")) {
			predicates.add(cb.lessThanOrEqualTo(invoice.get("totalAmount"), (Double) criteria.get("maxAmount")));
		}

		if (criteria.containsKey("startDate")) {
			predicates.add(cb.greaterThanOrEqualTo(invoice.get("invoiceDate"), (LocalDate) criteria.get("startDate")));
		}

		if (criteria.containsKey("endDate")) {
			predicates.add(cb.lessThanOrEqualTo(invoice.get("invoiceDate"), (LocalDate) criteria.get("endDate")));
		}

		query.where(predicates.toArray(new Predicate[0]));
		query.orderBy(cb.desc(invoice.get("invoiceDate")));

		TypedQuery<Invoice> typedQuery = entityManager.createQuery(query);

		// Add pagination if provided
		if (criteria.containsKey("page") && criteria.containsKey("size")) {
			int page = (Integer) criteria.get("page");
			int size = (Integer) criteria.get("size");
			typedQuery.setFirstResult(page * size);
			typedQuery.setMaxResults(size);
		}

		return typedQuery.getResultList();
	}

	@Override
	public Map<String, Object> getDashboardStatistics(User user, LocalDate startDate, LocalDate endDate) {
		Map<String, Object> stats = new HashMap<>();

		// Implement complex statistics calculation
		@SuppressWarnings("unused")
		String sql = "SELECT " + "COUNT(*) as totalInvoices, " + "SUM(total_amount) as totalRevenue, "
				+ "SUM(total_gst) as totalGst, " + "AVG(total_amount) as averageInvoiceValue, "
				+ "COUNT(DISTINCT customer_id) as uniqueCustomers " + "FROM invoices "
				+ "WHERE user_id = :userId AND invoice_date BETWEEN :startDate AND :endDate";

		// Execute native query and populate stats map
		// This is a simplified version - you'd need to implement the actual query
		// execution

		return stats;
	}

	@Override
	public List<Map<String, Object>> getSalesTrendAnalysis(User user, String periodType) {
		// Implement sales trend analysis
		List<Map<String, Object>> trends = new ArrayList<>();
		// Add implementation for sales trend analysis
		return trends;
	}
}