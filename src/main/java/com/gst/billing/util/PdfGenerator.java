package com.gst.billing.util;

import com.gst.billing.model.Invoice;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.OutputStream;

@Component
public class PdfGenerator {

	public void generateInvoice(Invoice invoice, OutputStream outputStream) {
		Document document = new Document();
		try {
			PdfWriter.getInstance(document, outputStream);
			document.open();

			// Invoice Header
			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
			Paragraph title = new Paragraph("TAX INVOICE", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			document.add(Chunk.NEWLINE);

			// Invoice Details
			PdfPTable headerTable = new PdfPTable(2);
			headerTable.setWidthPercentage(100);

			headerTable.addCell(createCell("Invoice Number: " + invoice.getInvoiceNumber(), false));
			headerTable.addCell(createCell("Date: " + invoice.getInvoiceDate().toString(), false));
			headerTable.addCell(createCell("Customer: " + invoice.getCustomer().getName(), false));
			headerTable.addCell(createCell("GSTIN: " + invoice.getCustomer().getGstin(), false));

			document.add(headerTable);
			document.add(Chunk.NEWLINE);

			// Items Table
			PdfPTable itemsTable = new PdfPTable(6);
			itemsTable.setWidthPercentage(100);
			itemsTable.setWidths(new float[] { 3, 4, 2, 2, 2, 3 });

			// Table Headers
			itemsTable.addCell(createCell("HSN Code", true));
			itemsTable.addCell(createCell("Product", true));
			itemsTable.addCell(createCell("Qty", true));
			itemsTable.addCell(createCell("Price", true));
			itemsTable.addCell(createCell("GST %", true));
			itemsTable.addCell(createCell("Amount", true));

			// Table Rows
			for (var item : invoice.getItems()) {
				itemsTable.addCell(createCell(item.getProduct().getHsnCode(), false));
				itemsTable.addCell(createCell(item.getProduct().getName(), false));
				itemsTable.addCell(createCell(String.valueOf(item.getQuantity()), false));
				itemsTable.addCell(createCell(item.getUnitPrice().toString(), false));
				itemsTable.addCell(createCell(item.getGstRate().toString(), false));
				itemsTable.addCell(createCell(item.getTotalAmount().toString(), false));
			}

			document.add(itemsTable);
			document.add(Chunk.NEWLINE);

			// Summary
			PdfPTable summaryTable = new PdfPTable(2);
			summaryTable.setWidthPercentage(50);
			summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

			summaryTable.addCell(createCell("Subtotal:", true));
			summaryTable.addCell(createCell(invoice.getSubtotal().toString(), false));
			summaryTable.addCell(createCell("Total GST:", true));
			summaryTable.addCell(createCell(invoice.getTotalGst().toString(), false));
			summaryTable.addCell(createCell("Total Amount:", true));
			summaryTable.addCell(createCell(invoice.getTotalAmount().toString(), true));

			document.add(summaryTable);

		} catch (DocumentException e) {
			throw new RuntimeException("Error generating PDF", e);
		} finally {
			document.close();
		}
	}

	private PdfPCell createCell(String content, boolean isHeader) {
		PdfPCell cell = new PdfPCell(new Phrase(content));
		cell.setPadding(5);
		if (isHeader) {
			cell.setBackgroundColor(new Color(0.9f, 0.9f, 0.9f));
		}
		return cell;
	}
}