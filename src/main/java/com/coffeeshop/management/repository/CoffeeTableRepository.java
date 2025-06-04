package com.coffeeshop.management.repository;

import com.coffeeshop.management.model.CoffeeTable;
import com.coffeeshop.management.model.CoffeeTable.TableStatus;
import com.coffeeshop.management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoffeeTableRepository extends JpaRepository<CoffeeTable, Long> {
    
    Optional<CoffeeTable> findByTableNumber(Integer tableNumber);
    
    List<CoffeeTable> findByStatus(TableStatus status);
    
    List<CoffeeTable> findByAssignedServer(User server);
    
    Boolean existsByTableNumber(Integer tableNumber);
}