package com.gst.billing.service;

import com.gst.billing.model.User;
import com.gst.billing.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

	@Autowired
	private InvoiceRepository invoiceRepository;

	/**
	 * Monthly Revenue Chart Data (User vs Admin)
	 */
	public List<String> getMonthLabels(User user, boolean isAdmin) {
		List<Object[]> results = isAdmin ? invoiceRepository.getMonthlyRevenueAll()
				: invoiceRepository.getMonthlyRevenue(user);

		List<String> labels = new ArrayList<>();
		for (Object[] row : results) {
			int monthNum = ((Number) row[0]).intValue();
			labels.add(Month.of(monthNum).name());
		}
		return labels;
	}

	public List<Double> getMonthRevenue(User user, boolean isAdmin) {
		List<Object[]> results = isAdmin ? invoiceRepository.getMonthlyRevenueAll()
				: invoiceRepository.getMonthlyRevenue(user);

		List<Double> revenue = new ArrayList<>();
		for (Object[] row : results) {
			revenue.add(((Number) row[1]).doubleValue());
		}
		return revenue;
	}

	/**
	 * Daily Revenue Chart Data (User vs Admin)
	 */
	public List<String> getDayLabels(User user, boolean isAdmin) {
		List<Object[]> results = isAdmin ? invoiceRepository.getDailyRevenueAll()
				: invoiceRepository.getDailyRevenue(user);

		List<String> labels = new ArrayList<>();
		for (Object[] row : results) {
			labels.add(row[0].toString()); // Format: YYYY-MM-DD
		}
		return labels;
	}

	public List<Double> getDayRevenue(User user, boolean isAdmin) {
		List<Object[]> results = isAdmin ? invoiceRepository.getDailyRevenueAll()
				: invoiceRepository.getDailyRevenue(user);

		List<Double> revenue = new ArrayList<>();
		for (Object[] row : results) {
			revenue.add(((Number) row[1]).doubleValue());
		}
		return revenue;
	}
}
