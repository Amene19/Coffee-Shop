package com.coffeeshop.management.repository;

import com.coffeeshop.management.model.CoffeeTable;
import com.coffeeshop.management.model.Order;
import com.coffeeshop.management.model.Order.OrderStatus;
import com.coffeeshop.management.model.Order.PaymentStatus;
import com.coffeeshop.management.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    
    List<Order> findByTable(CoffeeTable table);
    
    List<Order> findByServer(User server);
    
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.server = :server AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByServerAndDateRange(User server, LocalDateTime startDate, LocalDateTime endDate);
    
    Page<Order> findByOrderByCreatedAtDesc(Pageable pageable);
}