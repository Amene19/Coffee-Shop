package com.coffeeshop.management.controller;

import com.coffeeshop.management.model.CoffeeTable;
import com.coffeeshop.management.model.Order;
import com.coffeeshop.management.model.Order.OrderStatus;
import com.coffeeshop.management.model.OrderItem;
import com.coffeeshop.management.model.Product;
import com.coffeeshop.management.model.User;
import com.coffeeshop.management.security.UserDetailsImpl;
import com.coffeeshop.management.service.OrderService;
import com.coffeeshop.management.service.ProductService;
import com.coffeeshop.management.service.TableService;
import com.coffeeshop.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        if (userDetails == null) {
            // Should not happen with @PreAuthorize, but as a fallback
            return "redirect:/login";
        }

        // Get current user
        Optional<User> currentUserOptional = userService.findById(userDetails.getId());
        if (!currentUserOptional.isPresent()) {
            // User not found in database, maybe disabled or deleted
            return "redirect:/login?logout=true";
        }
        User currentUser = currentUserOptional.get();

        // Get tables assigned to this server
        List<CoffeeTable> assignedTables = tableService.findTablesByServer(currentUser.getId());
        if (assignedTables == null) assignedTables = new ArrayList<>();

        // Get orders ready for delivery
        List<Order> readyOrders = orderService.findOrdersByStatus(OrderStatus.READY);
         if (readyOrders == null) readyOrders = new ArrayList<>();
        
        // Get orders created by this server
        List<Order> myOrders = orderService.findOrdersByServer(currentUser);
         if (myOrders == null) myOrders = new ArrayList<>();
        
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
    public String viewTableDetails(@PathVariable Long id, Model model) {
        CoffeeTable table = tableService.findById(id)
            .orElseThrow(() -> new RuntimeException("Table not found with id: " + id));
        model.addAttribute("table", table);
        model.addAttribute("orders", orderService.findOrdersByTable(id));
        return "server/table-details";
    }
    
    @GetMapping("/orders/new")
    public String createOrderForm(Model model, @RequestParam(value = "tableId", required = false) Long tableId) {
        List<Product> products = productService.findAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("order", new Order());
        model.addAttribute("selectedTableId", tableId);
        model.addAttribute("tables", tableService.findAllTables());
        return "server/create-order";
    }

    @PostMapping("/orders/new")
    public String createOrder(@ModelAttribute("order") Order order,
                              @RequestParam(value = "productIds", required = false) List<Long> productIds,
                              @RequestParam(value = "quantities", required = false) List<Integer> quantities,
                              @RequestParam("tableId") Long tableId,
                              @AuthenticationPrincipal UserDetailsImpl userDetails,
                              RedirectAttributes redirectAttributes) {
        
        User server = userService.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Server user not found"));

        // Set the server and table for the order object
        order.setServer(server);
        if (tableId != null) {
            CoffeeTable table = tableService.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + tableId));
            order.setTable(table);
        }

        // Create the order first
        Order savedOrder = orderService.createOrder(order);

        // Add items to the order
        if (productIds != null && !productIds.isEmpty()) {
            for (int i = 0; i < productIds.size(); i++) {
                 if (quantities != null && i < quantities.size()) {
                    Long productId = productIds.get(i);
                    int quantity = quantities.get(i);
                    // Assuming no special instructions for now, can be added later
                    orderService.addItemToOrder(savedOrder.getId(), productId, quantity, "");
                 }
            }
        }
        
        redirectAttributes.addFlashAttribute("success", "Order created successfully!");
        return "redirect:/server/dashboard"; // Redirect to dashboard after creating order
    }

    @GetMapping("/orders")
    public String viewOrders(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        // Get current user
        Optional<User> currentUserOptional = userService.findById(userDetails.getId());
         if (!currentUserOptional.isPresent()) {
             return "redirect:/login?logout=true";
         }
         User currentUser = currentUserOptional.get();

        // Get orders created by this server
        List<Order> myOrders = orderService.findOrdersByServer(currentUser);
         if (myOrders == null) myOrders = new ArrayList<>();
        
        // Get orders ready for delivery
        List<Order> readyOrders = orderService.findOrdersByStatus(OrderStatus.READY);
         if (readyOrders == null) readyOrders = new ArrayList<>();
        
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

    @PostMapping("/tables/{id}/assign")
    public String assignTable(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails,
                              RedirectAttributes redirectAttributes) {
        User server = userService.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Server user not found"));
        tableService.assignServerToTable(id, server.getId());
        redirectAttributes.addFlashAttribute("success", "Table assigned successfully!");
        return "redirect:/server/tables";
    }

    @PostMapping("/tables/{id}/unassign")
    public String unassignTable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        tableService.assignServerToTable(id, null);
        redirectAttributes.addFlashAttribute("success", "Table unassigned successfully!");
        return "redirect:/server/tables";
    }

    @PostMapping("/tables/{id}/update-status")
    public String updateTableStatus(@PathVariable Long id, @RequestParam("status") CoffeeTable.TableStatus status,
                                    RedirectAttributes redirectAttributes) {
        tableService.updateTableStatus(id, status);
        redirectAttributes.addFlashAttribute("success", "Table status updated successfully!");
        return "redirect:/server/tables";
    }
}