package com.library.lms.security;

import com.library.lms.service.LogService;
import com.library.lms.web.FlashService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LogService logService;
    private final FlashService flashService;

    public LoginSuccessHandler(LogService logService, FlashService flashService) {
        this.logService = logService;
        this.flashService = flashService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        String username = authentication.getName();
        logService.record(username + " logged in");
        flashService.add(request, "success", "Welcome back, " + username + "!");
        try {
            response.sendRedirect(request.getContextPath() + "/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
