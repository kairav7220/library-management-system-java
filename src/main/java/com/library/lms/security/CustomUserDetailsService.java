package com.library.lms.security;

import com.library.lms.entity.User;
import com.library.lms.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));
        String userType = user.getUserType() == null ? "" : user.getUserType();
        String password = user.getPassword() == null ? "" : user.getPassword();
        boolean disabled = user.getStatus() != null && user.getStatus() == 1;
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(password)
                .roles(userType.isEmpty() ? "User" : userType)
                .disabled(disabled)
                .build();
    }
}
