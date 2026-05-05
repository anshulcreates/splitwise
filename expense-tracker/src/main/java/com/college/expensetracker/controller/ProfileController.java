package com.college.expensetracker.controller;

import com.college.expensetracker.model.User;
import com.college.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        try {
            userService.updateProfile(user, fullName, email, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmNewPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "New passwords do not match.");
            return "redirect:/profile";
        }
        if (newPassword.length() < 6 || !newPassword.matches(".*[A-Za-z].*") || !newPassword.matches(".*\\d.*")) {
            redirectAttributes.addFlashAttribute("passwordError", "Password must be at least 6 characters with a letter and a number.");
            return "redirect:/profile";
        }
        try {
            userService.changePassword(user, currentPassword, newPassword, passwordEncoder);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
