package com.coffeeshop.management.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private ERole name;
    
    public Role(ERole name) {
        this.name = name;
    }
    
    // Pre-defined roles enum
    public enum ERole {
        ROLE_ADMIN,
        ROLE_BARMAN,
        ROLE_SERVER,
        ROLE_CASHIER
    }
}