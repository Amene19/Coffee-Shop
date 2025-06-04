package com.coffeeshop.management.controller;

import com.coffeeshop.management.model.Order;
import com.coffeeshop.management.model.Order.OrderStatus;
import com.coffeeshop.management.model.Order.PaymentMethod;
import com.coffeeshop.management.model.Order.PaymentStatus;
import com.coffeeshop.management.security.UserDetailsImpl;
import com.coffeeshop.management.service.OrderService;
import com.coffeeshop.management.service.ReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

import static com.coffeeshop.management.model.Order.PaymentStatus.*;

@Controller
@RequestMapping("/cashier")
@PreAuthorize("hasRole('CASHIER')")
public class CashierController {
    
    private final OrderService orderService;
    private final ReceiptService receiptService;
    
    public CashierController(OrderService orderService, ReceiptService receiptService) {
        this.orderService = orderService;
        this.receiptService = receiptService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        // Get orders that need payment
        List<Order> deliveredOrders = orderService.findOrdersByStatusIn(
            List.of(OrderStatus.DELIVERED)
        );
        
        // Filter to only show unpaid orders
        List<Order> unpaidOrders = deliveredOrders.stream()
            .filter(order -> order.getPaymentStatus() == UNPAID)
            .toList();
        
        // Get recently paid orders

        
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("unpaidOrders", unpaidOrders);

        
        return "cashier/dashboard";
    }
    
    @GetMapping("/orders")
    public String viewOrders(Model model) {
        // Get all delivered orders
        List<Order> deliveredOrders = orderService.findOrdersByStatus(OrderStatus.DELIVERED);
        
        model.addAttribute("deliveredOrders", deliveredOrders);
        return "cashier/order-list";
    }
    
    @GetMapping("/orders/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        model.addAttribute("order", order);
        return "cashier/order-details";
    }
    
    @GetMapping("/orders/{id}/payment")
    public String showPaymentForm(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        model.addAttribute("order", order);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        
        return "cashier/payment-form";
    }
    
    @PostMapping("/orders/{id}/discount")
    public String applyDiscount(@PathVariable Long id, 
                             @RequestParam BigDecimal discountAmount,
                             RedirectAttributes redirectAttributes) {
        
        Order order = orderService.applyDiscount(id, discountAmount);
        
        redirectAttributes.addFlashAttribute("success", 
            "Discount of $" + discountAmount + " applied to Order #" + order.getOrderNumber());
        
        return "redirect:/cashier/orders/" + id + "/payment";
    }
    
    @PostMapping("/orders/{id}/complete-payment")
    public String completePayment(@PathVariable Long id, 
                                @RequestParam PaymentMethod paymentMethod,
                                RedirectAttributes redirectAttributes) {
        
        Order order = orderService.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        order.setPaymentStatus(PAID);
        order.setPaymentMethod(paymentMethod);
        orderService.updateOrder(order);
        
        redirectAttributes.addFlashAttribute("success", 
            "Payment completed for Order #" + order.getOrderNumber());
        
        return "redirect:/cashier/orders";
    }
    
    @GetMapping("/orders/{id}/receipt")
    public ResponseEntity<byte[]> generateReceipt(@PathVariable Long id) {
        Order order = orderService.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        byte[] pdfBytes = receiptService.generateReceiptPdf(order);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt-" + order.getOrderNumber() + ".pdf");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
    
    @GetMapping("/orders/{id}/receipt/html")
    public String showHtmlReceipt(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        model.addAttribute("order", order);
        return "cashier/receipt";
    }
}