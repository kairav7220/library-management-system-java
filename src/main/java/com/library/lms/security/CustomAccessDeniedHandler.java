package com.library.lms.security;

import com.library.lms.web.FlashService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final FlashService flashService;

    public CustomAccessDeniedHandler(FlashService flashService) {
        this.flashService = flashService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            flashService.add(request, "warning", "Your session or security token was invalid. Please log in again.");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            flashService.add(request, "error", "You do not have permission to access that resource.");
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
