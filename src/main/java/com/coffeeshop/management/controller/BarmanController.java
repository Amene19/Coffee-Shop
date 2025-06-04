package com.coffeeshop.management.controller;

import com.coffeeshop.management.model.Order;
import com.coffeeshop.management.model.Order.OrderStatus;
import com.coffeeshop.management.model.OrderItem;
import com.coffeeshop.management.security.UserDetailsImpl;
import com.coffeeshop.management.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/barman")
@PreAuthorize("hasRole('BARMAN')")
public class BarmanController {
    
    private final OrderService orderService;
    
    public BarmanController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        // Get orders that need to be prepared
        List<Order> pendingOrders = orderService.findOrdersByStatus(OrderStatus.PENDING);
        List<Order> inProgressOrders = orderService.findOrdersByStatus(OrderStatus.IN_PROGRESS);
        List<Order> readyOrders = orderService.findOrdersByStatus(OrderStatus.READY);
        
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("inProgressOrders", inProgressOrders);
        model.addAttribute("readyOrders", readyOrders);
        
        return "barman/dashboard";
    }
    
    @GetMapping("/orders")
    public String viewOrders(Model model) {
        // Get all active orders (pending, in progress, ready)
        List<Order> activeOrders = orderService.findOrdersByStatusIn(
            Arrays.asList(OrderStatus.PENDING, OrderStatus.IN_PROGRESS, OrderStatus.READY)
        );
        
        model.addAttribute("activeOrders", activeOrders);
        return "barman/order-list";
    }
    
    @GetMapping("/orders/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        model.addAttribute("order", order);
        return "barman/order-details";
    }
    
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, 
                                  @RequestParam OrderStatus status,
                                  RedirectAttributes redirectAttributes) {
        
        Order order = orderService.updateOrderStatus(id, status);
        
        redirectAttributes.addFlashAttribute("success", 
            "Order #" + order.getOrderNumber() + " status updated to " + status);
        
        return "redirect:/barman/orders";
    }
    
    @PostMapping("/orders/{orderId}/items/{itemId}/status")
    public String updateItemStatus(@PathVariable Long orderId,
                                 @PathVariable Long itemId,
                                 @RequestParam OrderItem.ItemStatus status,
                                 RedirectAttributes redirectAttributes) {
        
        Order order = orderService.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        order.getItems().stream()
            .filter(item -> item.getId().equals(itemId))
            .findFirst()
            .ifPresent(item -> item.setStatus(status));
        
        orderService.updateOrder(order);
        
        redirectAttributes.addFlashAttribute("success", "Item status updated to " + status);
        
        return "redirect:/barman/orders/" + orderId;
    }
}