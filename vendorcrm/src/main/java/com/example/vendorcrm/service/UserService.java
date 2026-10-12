
package com.example.vendorcrm.service;

import com.example.vendorcrm.entity.User;
import com.example.vendorcrm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Create user
    public User saveUser(User user) {

        if (user.getEmail() == null ||
                user.getEmail().isBlank()) {

            throw new RuntimeException(
                    "Email cannot be empty");
        }

        if (userRepository.existsByEmailIgnoreCase(
                user.getEmail())) {

            throw new RuntimeException(
                    "Email already exists");
        }

        return userRepository.save(user);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    // Delete user
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new RuntimeException(
                    "User not found with ID: " + id);
        }

        userRepository.deleteById(id);
    }
}
