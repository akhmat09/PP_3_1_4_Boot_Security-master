package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.entity.Role;
import java.util.List;

public interface RoleService {

    Role findByName(String name);

    List<Role> getAllRoles();

    void saveRole(Role role);

    boolean existsByName(String name);
}