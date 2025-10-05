package com.gst.billing.repository;

import com.gst.billing.model.Invoice;
import com.gst.billing.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InvoiceRepositoryCustom {

	List<Invoice> findInvoicesWithComplexCriteria(User user, Map<String, Object> criteria);

	Map<String, Object> getDashboardStatistics(User user, LocalDate startDate, LocalDate endDate);

	List<Map<String, Object>> getSalesTrendAnalysis(User user, String periodType);
}