package com.coffeeshop.management.controller;

import com.coffeeshop.management.model.Role;
import com.coffeeshop.management.model.User;
import com.coffeeshop.management.security.UserDetailsImpl;
import com.coffeeshop.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

@Controller
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Redirect based on role
        if (userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_BARMAN"))) {
            return "redirect:/barman/dashboard";
        } else if (userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SERVER"))) {
            return "redirect:/server/dashboard";
        } else if (userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CASHIER"))) {
            return "redirect:/cashier/dashboard";
        }
        
        return "redirect:/login";
    }
    
    @GetMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
                              @ModelAttribute("roles") String[] roles,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username is already taken");
            return "auth/register";
        }
        
        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email is already in use");
            return "auth/register";
        }
        
        Set<Role.ERole> roleSet = new HashSet<>();
        for (String role : roles) {
            try {
                roleSet.add(Role.ERole.valueOf(role));
            } catch (IllegalArgumentException e) {
                // Invalid role, ignore
            }
        }
        
        // If no roles selected, default to SERVER
        if (roleSet.isEmpty()) {
            roleSet.add(Role.ERole.ROLE_SERVER);
        }
        
        userService.createUser(user, roleSet);
        
        redirectAttributes.addFlashAttribute("success", "User registered successfully!");
        return "redirect:/admin/users";
    }
}