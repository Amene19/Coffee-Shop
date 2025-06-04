package com.coffeeshop.management.controller;

import com.coffeeshop.management.model.CoffeeTable;
import com.coffeeshop.management.model.Order;
import com.coffeeshop.management.model.Order.OrderStatus;
import com.coffeeshop.management.model.Product;
import com.coffeeshop.management.model.User;
import com.coffeeshop.management.security.UserDetailsImpl;
import com.coffeeshop.management.service.OrderService;
import com.coffeeshop.management.service.ProductService;
import com.coffeeshop.management.service.TableService;
import com.coffeeshop.management.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/server")
@PreAuthorize("hasRole('SERVER')")
public class ServerController {
    
    private final OrderService orderService;
    private final TableService tableService;
    private final ProductService productService;
    private final UserService userService;
    
    public ServerController(OrderService orderService, TableService tableService,
                          ProductService productService, UserService userService) {
        this.orderService = orderService;
        this.tableService = tableService;
        this.productService = productService;
        this.userService = userService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        // Get current user
        User currentUser = userService.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get tables assigned to this server
        List<CoffeeTable> assignedTables = tableService.findTablesByServer(currentUser.getId());
        
        // Get orders ready for delivery
        List<Order> readyOrders = orderService.findOrdersByStatus(OrderStatus.READY);
        
        // Get orders created by this server
        List<Order> myOrders = orderService.findOrdersByServer(currentUser);
        
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("assignedTables", assignedTables);
        model.addAttribute("readyOrders", readyOrders);
        model.addAttribute("myOrders", myOrders);
        
        return "server/dashboard";
    }
    
    @GetMapping("/tables")
    public String viewTables(Model model) {
        List<CoffeeTable> tables = tableService.findAllTables();
        model.addAttribute("tables", tables);
        return "server/table-list";
    }
    
    @GetMapping("/tables/{id}")
    public String viewTable(@PathVariable Long id, Model model) {
        CoffeeTable table = tableService.findById(id)
            .orElseThrow(() -> new RuntimeException("Table not found with id: " + id));
        
        List<Order> tableOrders = orderService.findOrdersByTable(id);
        
        model.addAttribute("table", table);
        model.addAttribute("tableOrders", tableOrders);
        return "server/table-details";
    }
    
    @GetMapping("/orders/new")
    public String showCreateOrderForm(Model model, @RequestParam(required = false) Long tableId) {
        List<CoffeeTable> availableTables = tableService.findTablesByStatus(CoffeeTable.TableStatus.AVAILABLE);
        List<Product> availableProducts = productService.findAvailableProducts();
        
        model.addAttribute("order", new Order());
        model.addAttribute("tables", availableTables);
        model.addAttribute("products", availableProducts);
        
        if (tableId != null) {
            model.addAttribute("selectedTableId", tableId);
        }
        
        return "server/order-form";
    }
    
    @PostMapping("/orders/save")
    public String createOrder(@ModelAttribute Order order, 
                            @AuthenticationPrincipal UserDetailsImpl userDetails,
                            @RequestParam Long tableId,
                            @RequestParam(required = false) List<Long> productIds,
                            @RequestParam(required = false) List<Integer> quantities,
                            @RequestParam(required = false) List<String> specialInstructions,
                            RedirectAttributes redirectAttributes) {
        
        // Set the server
        User currentUser = userService.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        order.setServer(currentUser);
        
        // Set the table
        CoffeeTable table = tableService.findById(tableId)
            .orElseThrow(() -> new RuntimeException("Table not found with id: " + tableId));
        order.setTable(table);
        
        // Create the order first
        Order savedOrder = orderService.createOrder(order);
        
        // Add items to the order
        if (productIds != null && !productIds.isEmpty()) {
            for (int i = 0; i < productIds.size(); i++) {
                Long productId = productIds.get(i);
                int quantity = quantities.get(i);
                String instruction = specialInstructions != null && i < specialInstructions.size() ? 
                    specialInstructions.get(i) : "";
                
                orderService.addItemToOrder(savedOrder.getId(), productId, quantity, instruction);
            }
        }
        
        redirectAttributes.addFlashAttribute("success", 
            "Order #" + savedOrder.getOrderNumber() + " created successfully!");
        
        return "redirect:/server/tables/" + tableId;
    }
    
    @GetMapping("/orders")
    public String viewOrders(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        // Get current user
        User currentUser = userService.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get orders created by this server
        List<Order> myOrders = orderService.findOrdersByServer(currentUser);
        
        // Get orders ready for delivery
        List<Order> readyOrders = orderService.findOrdersByStatus(OrderStatus.READY);
        
        model.addAttribute("myOrders", myOrders);
        model.addAttribute("readyOrders", readyOrders);
        
        return "server/order-list";
    }
    
    @PostMapping("/orders/{id}/deliver")
    public String deliverOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Order order = orderService.updateOrderStatus(id, OrderStatus.DELIVERED);
        
        redirectAttributes.addFlashAttribute("success", 
            "Order #" + order.getOrderNumber() + " marked as delivered!");
        
        return "redirect:/server/orders";
    }
}