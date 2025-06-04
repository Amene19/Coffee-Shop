package com.coffeeshop.management.service;

import com.coffeeshop.management.model.CoffeeTable;
import com.coffeeshop.management.model.User;
import com.coffeeshop.management.repository.CoffeeTableRepository;
import com.coffeeshop.management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TableService {
    
    private final CoffeeTableRepository tableRepository;
    private final UserRepository userRepository;
    
    public TableService(CoffeeTableRepository tableRepository, UserRepository userRepository) {
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
    }
    
    public List<CoffeeTable> findAllTables() {
        return tableRepository.findAll();
    }
    
    public List<CoffeeTable> findTablesByStatus(CoffeeTable.TableStatus status) {
        return tableRepository.findByStatus(status);
    }
    
    public List<CoffeeTable> findTablesByServer(Long serverId) {
        User server = userRepository.findById(serverId)
            .orElseThrow(() -> new RuntimeException("Server not found with id: " + serverId));
        
        return tableRepository.findByAssignedServer(server);
    }
    
    public Optional<CoffeeTable> findById(Long id) {
        return tableRepository.findById(id);
    }
    
    public Optional<CoffeeTable> findByTableNumber(Integer tableNumber) {
        return tableRepository.findByTableNumber(tableNumber);
    }
    
    @Transactional
    public CoffeeTable createTable(CoffeeTable table) {
        if (tableRepository.existsByTableNumber(table.getTableNumber())) {
            throw new RuntimeException("Table number already exists: " + table.getTableNumber());
        }
        
        return tableRepository.save(table);
    }
    
    @Transactional
    public CoffeeTable updateTable(CoffeeTable table) {
        return tableRepository.save(table);
    }
    
    @Transactional
    public void deleteTable(Long id) {
        tableRepository.deleteById(id);
    }
    
    @Transactional
    public CoffeeTable assignServerToTable(Long tableId, Long serverId) {
        CoffeeTable table = tableRepository.findById(tableId)
            .orElseThrow(() -> new RuntimeException("Table not found with id: " + tableId));
        
        User server = userRepository.findById(serverId)
            .orElseThrow(() -> new RuntimeException("Server not found with id: " + serverId));
        
        table.setAssignedServer(server);
        
        return tableRepository.save(table);
    }
    
    @Transactional
    public CoffeeTable updateTableStatus(Long tableId, CoffeeTable.TableStatus status) {
        CoffeeTable table = tableRepository.findById(tableId)
            .orElseThrow(() -> new RuntimeException("Table not found with id: " + tableId));
        
        table.setStatus(status);
        
        return tableRepository.save(table);
    }
}