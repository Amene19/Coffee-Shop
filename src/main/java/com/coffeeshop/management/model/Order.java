package com.coffeeshop.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderNumber;
    
    @ManyToOne
    @JoinColumn(name = "table_id")
    private CoffeeTable table;
    
    @ManyToOne
    @JoinColumn(name = "server_id", nullable = false)
    private User server;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private BigDecimal tax = BigDecimal.ZERO;
    
    private BigDecimal discount = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private BigDecimal total = BigDecimal.ZERO;
    
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    private String notes;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Helper method to add an item
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateOrderTotals();
    }
    
    // Helper method to remove an item
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateOrderTotals();
    }
    
    // Helper method to calculate totals
    public void recalculateOrderTotals() {
        this.subtotal = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Assuming tax rate of 10%
        this.tax = this.subtotal.multiply(new BigDecimal("0.10"));
        
        // Total = subtotal + tax - discount
        this.total = this.subtotal.add(this.tax).subtract(this.discount);
    }
    
    // Order status enum
    public enum OrderStatus {
        PENDING,
        IN_PROGRESS,
        READY,
        DELIVERED,
        CANCELLED
    }
    
    // Payment status enum
    public enum PaymentStatus {
        UNPAID,
        PAID,
        REFUNDED
    }
    
    // Payment method enum
    public enum PaymentMethod {
        CASH,
        CREDIT_CARD,
        DEBIT_CARD,
        MOBILE_PAYMENT
    }
}