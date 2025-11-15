package ru.kata.spring.boot_security.demo.util;

import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initUsers();
    }

    private void initRoles() {
        if (roleService.findByName("ROLE_ADMIN") == null) {
            roleService.saveRole(new Role("ROLE_ADMIN"));
        }

        if (roleService.findByName("ROLE_USER") == null) {
            roleService.saveRole(new Role("ROLE_USER"));
        }
    }

    private void initUsers() {
        if (userService.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@example.com");
            admin.setFirstName("Admin");
            admin.setLastName("Adminov");
            admin.setAge(35);

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleService.findByName("ROLE_ADMIN"));
            adminRoles.add(roleService.findByName("ROLE_USER"));
            admin.setRoles(adminRoles);

            userService.saveUser(admin);
        }

        if (userService.findByUsername("user") == null) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user"));
            user.setEmail("user@example.com");
            user.setFirstName("User");
            user.setLastName("Userov");
            user.setAge(25);

            Set<Role> userRoles = new HashSet<>();
            userRoles.add(roleService.findByName("ROLE_USER"));
            user.setRoles(userRoles);

            userService.saveUser(user);
        }
    }
}