package com.coffeeshop.management.controller;

import com.coffeeshop.management.model.Category;
import com.coffeeshop.management.model.Order;
import com.coffeeshop.management.model.Product;
import com.coffeeshop.management.model.User;
import com.coffeeshop.management.security.UserDetailsImpl;
import com.coffeeshop.management.service.CategoryService;
import com.coffeeshop.management.service.OrderService;
import com.coffeeshop.management.service.ProductService;
import com.coffeeshop.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    
    public AdminController(UserService userService, ProductService productService,
                         CategoryService categoryService, OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        // Get recent orders
        Page<Order> recentOrders = orderService.findRecentOrders(PageRequest.of(0, 5));
        
        // Get best selling products
        List<Product> bestSellingProducts = productService.findBestSellingProducts(5);
        
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("bestSellingProducts", bestSellingProducts);
        
        return "admin/dashboard";
    }
    
    // User Management
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/user-list";
    }
    
    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/user-form";
    }
    
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        model.addAttribute("user", user);
        return "admin/user-form";
    }
    
    // Product Management
    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.findAllProducts();
        List<Category> categories = categoryService.findAllCategories();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "admin/product-list";
    }
    
    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        List<Category> categories = categoryService.findAllCategories();
        
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categories);
        return "admin/product-form";
    }
    
    @PostMapping("/products/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                            BindingResult result, RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/product-form";
        }
        
        if (product.getId() == null) {
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
        } else {
            productService.updateProduct(product);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        }
        
        return "redirect:/admin/products";
    }
    
    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        List<Category> categories = categoryService.findAllCategories();
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "admin/product-form";
    }
    
    @GetMapping("/products/toggle/{id}")
    public String toggleProductAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Product product = productService.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        productService.changeProductAvailability(id, !product.isAvailable());
        
        String status = product.isAvailable() ? "unavailable" : "available";
        redirectAttributes.addFlashAttribute("success", 
            "Product " + product.getName() + " is now " + status);
        
        return "redirect:/admin/products";
    }
    
    // Category Management
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("category", new Category());
        return "admin/category-list";
    }
    
    // Reports
    @GetMapping("/reports")
    public String showReports(Model model,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate) {
        
        LocalDateTime start = startDate != null ? 
            LocalDateTime.parse(startDate + "T00:00:00") : 
            LocalDateTime.now().minusDays(30);
            
        LocalDateTime end = endDate != null ? 
            LocalDateTime.parse(endDate + "T23:59:59") : 
            LocalDateTime.now();
        
        // Sales by period
        List<Order> periodOrders = orderService.findOrdersByDateRange(start, end);
        
        // Best selling products
        List<Product> bestSellingProducts = productService.findBestSellingProducts(10);
        
        model.addAttribute("periodOrders", periodOrders);
        model.addAttribute("bestSellingProducts", bestSellingProducts);
        model.addAttribute("startDate", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("endDate", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        return "admin/reports";
    }
}