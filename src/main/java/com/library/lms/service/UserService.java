package com.library.lms.service;

import com.library.lms.entity.User;
import com.library.lms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final IdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, IdGenerator idGenerator, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.idGenerator = idGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getByRowNum(Integer rowNum) {
        return userRepository.findById(rowNum);
    }

    public List<User> listActive() {
        return userRepository.findByStatus(0);
    }

    public List<User> search(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        return listActive().stream()
                .filter(u -> contains(u.getUsername(), q) || contains(u.getEmail(), q)
                        || contains(u.getUserType(), q) || contains(u.getUserId(), q))
                .toList();
    }

    @Transactional
    public User create(String userType, String username, String rawPassword, String email, String phone) {
        Validators.requireNonBlank("User type", userType);
        Validators.requireNonBlank("Username", username);
        Validators.requireNonBlank("Password", rawPassword);
        Validators.requireEmail("Email", email);
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("That username already exists.");
        }
        User user = new User();
        user.setUserId(idGenerator.nextId("USER", userRepository.findAll(), User::getRowNum));
        user.setUserType(userType);
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(0);
        return userRepository.save(user);
    }

    @Transactional
    public User update(Integer rowNum, String userType, String username, String rawPassword,
                       String email, String phone) {
        User user = userRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        userRepository.findByUsername(username.trim())
                .filter(existing -> !existing.getRowNum().equals(rowNum))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("That username already exists.");
                });
        user.setUserType(userType);
        user.setUsername(username.trim());
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        user.setEmail(email);
        user.setPhone(phone);
        return userRepository.save(user);
    }

    @Transactional
    public void softDelete(Integer rowNum) {
        User user = userRepository.findById(rowNum)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setStatus(1);
        userRepository.save(user);
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }
}
