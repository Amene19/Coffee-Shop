package com.coffeeshop.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;
    
    @NotNull
    @Column(nullable = false)
    private BigDecimal price;
    
    private String specialInstructions;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.PENDING;
    
    // Calculate subtotal
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    // Item status enum
    public enum ItemStatus {
        PENDING,
        IN_PROGRESS,
        READY,
        DELIVERED,
        CANCELLED
    }
}