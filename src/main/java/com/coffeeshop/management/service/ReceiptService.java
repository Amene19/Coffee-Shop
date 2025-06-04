package com.coffeeshop.management.service;

import com.coffeeshop.management.model.Order;
import com.coffeeshop.management.model.OrderItem;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class ReceiptService {
    
    public byte[] generateReceiptPdf(Order order) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, baos);
            
            document.open();
            addReceiptContent(document, order);
            document.close();
            
            return baos.toByteArray();
            
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF receipt", e);
        }
    }
    
    private void addReceiptContent(Document document, Order order) throws DocumentException {
        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Coffee Shop Receipt", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);
        
        // Add order info
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        
        document.add(new Paragraph("Order #: " + order.getOrderNumber(), boldFont));
        document.add(new Paragraph("Date: " + 
            order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), normalFont));
        document.add(new Paragraph("Table: " + 
            (order.getTable() != null ? order.getTable().getTableNumber() : "N/A"), normalFont));
        document.add(new Paragraph("Server: " + order.getServer().getFullName(), normalFont));
        document.add(Chunk.NEWLINE);
        
        // Add items table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        
        // Set column widths
        table.setWidths(new float[]{2, 0.5f, 1, 1});
        
        // Add table header
        addTableHeader(table);
        
        // Add items
        for (OrderItem item : order.getItems()) {
            addTableRow(table, item);
        }
        
        document.add(table);
        document.add(Chunk.NEWLINE);
        
        // Add totals
        document.add(new Paragraph("Subtotal: $" + order.getSubtotal().toString(), normalFont));
        document.add(new Paragraph("Tax: $" + order.getTax().toString(), normalFont));
        
        if (order.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            document.add(new Paragraph("Discount: -$" + order.getDiscount().toString(), normalFont));
        }
        
        document.add(new Paragraph("Total: $" + order.getTotal().toString(), boldFont));
        document.add(Chunk.NEWLINE);
        
        // Add payment info
        document.add(new Paragraph("Payment Method: " + 
            (order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : "N/A"), normalFont));
        document.add(new Paragraph("Payment Status: " + order.getPaymentStatus().toString(), normalFont));
        document.add(Chunk.NEWLINE);
        
        // Add thank you message
        Paragraph thanks = new Paragraph("Thank you for your visit!", boldFont);
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);
    }
    
    private void addTableHeader(PdfPTable table) {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        
        PdfPCell cell;
        
        cell = new PdfPCell(new Phrase("Item", headerFont));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Qty", headerFont));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Price", headerFont));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
        
        cell = new PdfPCell(new Phrase("Subtotal", headerFont));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private void addTableRow(PdfPTable table, OrderItem item) {
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
        
        table.addCell(new Phrase(item.getProduct().getName(), cellFont));
        table.addCell(new Phrase(String.valueOf(item.getQuantity()), cellFont));
        table.addCell(new Phrase("$" + item.getPrice().toString(), cellFont));
        table.addCell(new Phrase("$" + item.getSubtotal().toString(), cellFont));
    }
}