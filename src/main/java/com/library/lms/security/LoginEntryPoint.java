package com.library.lms.security;

import com.library.lms.web.FlashService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class LoginEntryPoint implements AuthenticationEntryPoint {

    private final FlashService flashService;

    public LoginEntryPoint(FlashService flashService) {
        this.flashService = flashService;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        flashService.add(request, "warning", "Please log in to continue.");
        try {
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
