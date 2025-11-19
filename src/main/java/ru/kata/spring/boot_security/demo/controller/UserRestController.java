package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dto.UserDto;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.service.UserService;
import ru.kata.spring.boot_security.demo.service.RoleService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public UserRestController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        try {
            User user = convertToEntity(userDto);
            userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        try {
            User user = convertToEntity(userDto);
            user.setId(id);
            userService.updateUser(id, user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating user: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();

            List<Map<String, Object>> roleDtos = roles.stream()
                    .map(role -> {
                        Map<String, Object> roleMap = new HashMap<>();
                        roleMap.put("id", role.getId());
                        roleMap.put("name", role.getName());
                        return roleMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(roleDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<User> getCurrentUser(java.security.Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(user);
    }

    private User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setAge(userDto.getAge());

        Set<Role> roles = new HashSet<>();
        if (userDto.getRoleIds() != null && !userDto.getRoleIds().isEmpty()) {
            for (Long roleId : userDto.getRoleIds()) {
                Role role = roleService.getAllRoles().stream()
                        .filter(r -> r.getId().equals(roleId))
                        .findFirst()
                        .orElse(null);
                if (role != null) {
                    roles.add(role);
                }
            }
        }

        if (roles.isEmpty()) {
            Role defaultRole = roleService.findByName("ROLE_USER");
            if (defaultRole != null) {
                roles.add(defaultRole);
            }
        }

        user.setRoles(roles);
        return user;
    }
}