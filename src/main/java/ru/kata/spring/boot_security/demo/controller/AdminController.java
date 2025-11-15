package ru.kata.spring.boot_security.demo.controller;

import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.service.UserService;
import ru.kata.spring.boot_security.demo.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/admin-panel";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users-list";
    }

    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user,
                             @RequestParam(value = "selectedRoles", required = false) Set<Long> roleIds) {
        setUserRoles(user, roleIds);
        userService.saveUser(user);
        return "redirect:/admin/users?success=true";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "admin/user-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user,
                             @RequestParam(value = "selectedRoles", required = false) Set<Long> roleIds) {
        setUserRoles(user, roleIds);
        userService.updateUser(id, user);
        return "redirect:/admin/users?updated=true";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users?deleted=true";
    }

    private void setUserRoles(User user, Set<Long> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : roleIds) {
                Role role = roleService.getAllRoles().stream()
                        .filter(r -> r.getId().equals(roleId))
                        .findFirst()
                        .orElse(null);
                if (role != null) {
                    roles.add(role);
                }
            }
            user.setRoles(roles);
        } else {
            // Если роли не выбраны, устанавливаем роль USER по умолчанию
            Role userRole = roleService.findByName("ROLE_USER");
            if (userRole != null) {
                user.setRoles(Set.of(userRole));
            }
        }
    }
}