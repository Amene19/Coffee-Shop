package com.coffeeshop.management;

import com.coffeeshop.management.model.Role;
import com.coffeeshop.management.model.Role.ERole;
import com.coffeeshop.management.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DataInitializer {
    
    /**
     * Initialize required roles if they don't exist.
     * This runs only when not using H2 database (where data.sql is used instead).
     */
    @Bean
    @Profile("!h2")
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            // Check if we need to create default roles
            if (roleRepository.count() == 0) {
                System.out.println("Initializing default roles...");
                
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
                roleRepository.save(new Role(ERole.ROLE_BARMAN));
                roleRepository.save(new Role(ERole.ROLE_SERVER));
                roleRepository.save(new Role(ERole.ROLE_CASHIER));
                
                System.out.println("Default roles created!");
            }
        };
    }
}