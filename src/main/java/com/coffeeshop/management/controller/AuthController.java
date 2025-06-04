package com.coffeeshop.management.controller;

import com.coffeeshop.management.model.Role;
import com.coffeeshop.management.model.User;
import com.coffeeshop.management.security.UserDetailsImpl;
import com.coffeeshop.management.service.UserService;
import com.coffeeshop.management.repository.RoleRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    
    public AuthController(UserService userService, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
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
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
                              @ModelAttribute("role") String role,
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
        
        // Set the selected role
        Set<Role.ERole> roleSet = new HashSet<>();
        try {
            Role.ERole selectedRole = Role.ERole.valueOf(role);
            if (selectedRole == Role.ERole.ROLE_ADMIN) {
                result.rejectValue("role", "error.role", "Cannot register as admin");
                return "auth/register";
            }
            roleSet.add(selectedRole);
        } catch (IllegalArgumentException e) {
            result.rejectValue("role", "error.role", "Invalid role selected");
            return "auth/register";
        }
        
        userService.createUser(user, roleSet);
        
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
        return "redirect:/login";
    }
}