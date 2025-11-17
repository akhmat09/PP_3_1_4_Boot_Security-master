package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.entity.User;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User findByUsername(String username);
    void saveUser(User user);
    void updateUser(Long id, User user);
    void deleteUser(Long id);
}