package com.coffeeshop.management.service;

import com.coffeeshop.management.model.CoffeeTable;
import com.coffeeshop.management.model.Order;
import com.coffeeshop.management.model.Order.OrderStatus;
import com.coffeeshop.management.model.OrderItem;
import com.coffeeshop.management.model.Product;
import com.coffeeshop.management.model.User;
import com.coffeeshop.management.repository.CoffeeTableRepository;
import com.coffeeshop.management.repository.OrderRepository;
import com.coffeeshop.management.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CoffeeTableRepository tableRepository;
    private final ProductRepository productRepository;
    
    public OrderService(OrderRepository orderRepository, CoffeeTableRepository tableRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.productRepository = productRepository;
    }
    
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }
    
    public Page<Order> findRecentOrders(Pageable pageable) {
        return orderRepository.findByOrderByCreatedAtDesc(pageable);
    }
    
    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    public List<Order> findOrdersByStatusIn(List<OrderStatus> statuses) {
        return orderRepository.findByStatusIn(statuses);
    }
    
    public List<Order> findOrdersByTable(Long tableId) {
        CoffeeTable table = tableRepository.findById(tableId)
            .orElseThrow(() -> new RuntimeException("Table not found with id: " + tableId));
        
        return orderRepository.findByTable(table);
    }
    
    public List<Order> findOrdersByServer(User server) {
        return orderRepository.findByServer(server);
    }
    
    public List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersByDateRange(startDate, endDate);
    }
    
    public List<Order> findOrdersByServerAndDateRange(User server, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersByServerAndDateRange(server, startDate, endDate);
    }
    
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    @Transactional
    public Order createOrder(Order order) {
        // Generate a unique order number
        String orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);
        
        // Update table status if table is assigned
        if (order.getTable() != null) {
            CoffeeTable table = order.getTable();
            table.setStatus(CoffeeTable.TableStatus.OCCUPIED);
            tableRepository.save(table);
        }
        
        // Recalculate totals
        order.recalculateOrderTotals();
        
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order updateOrder(Order order) {
        order.recalculateOrderTotals();
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order addItemToOrder(Long orderId, Long productId, int quantity, String specialInstructions) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());
        item.setSpecialInstructions(specialInstructions);
        
        order.addItem(item);
        
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order removeItemFromOrder(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        order.getItems().removeIf(item -> item.getId().equals(itemId));
        order.recalculateOrderTotals();
        
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        order.setStatus(status);
        
        // If order is completed, set completion time
        if (status == OrderStatus.DELIVERED) {
            order.setCompletedAt(LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order applyDiscount(Long orderId, BigDecimal discountAmount) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        order.setDiscount(discountAmount);
        order.recalculateOrderTotals();
        
        return orderRepository.save(order);
    }
    
    private String generateOrderNumber() {
        // Format: ORD-YYYYMMDD-XXXX (where XXXX is random)
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        return "ORD-" + datePart + "-" + randomPart;
    }
}