package com.library.lms.web;

import com.library.lms.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class WebAdvice {

    private final FlashService flashService;
    private final UserRepository userRepository;

    public WebAdvice(FlashService flashService, UserRepository userRepository) {
        this.flashService = flashService;
        this.userRepository = userRepository;
    }

    @org.springframework.web.bind.annotation.ModelAttribute
    public void enrich(jakarta.servlet.http.HttpServletRequest request,
                       org.springframework.ui.Model model) {
        model.addAttribute("flashMessages", flashService.drain(request));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            model.addAttribute("currentUsername", auth.getName());
            String userType = userRepository.findByUsername(auth.getName())
                    .map(u -> u.getUserType() == null ? "" : u.getUserType())
                    .orElse("");
            model.addAttribute("currentUserType", userType);
        } else {
            model.addAttribute("currentUsername", "");
            model.addAttribute("currentUserType", "");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        flashService.add(request, "error", ex.getMessage());
        return "redirect:" + safeBack(request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String detail = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        String message;
        if (detail.contains("duplicate")) {
            message = "That value already exists.";
        } else if (detail.contains("cannot be null") || detail.contains("notnull") || detail.contains("not null")) {
            message = "A required field is missing.";
        } else {
            message = "That would conflict with existing data.";
        }
        flashService.add(request, "error", message);
        return "redirect:" + safeBack(request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        flashService.add(request, "error", "A value you entered is not in the right format.");
        return "redirect:" + safeBack(request);
    }

    private String safeBack(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String context = request.getContextPath() == null ? "" : request.getContextPath();
        if (referer != null && referer.startsWith(request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length() + context.length()) + "/")) {
            return referer.substring(request.getRequestURL().length() - request.getRequestURI().length() + context.length());
        }
        if (referer != null && context.isEmpty() && referer.matches("^https?://[^/]+/.*")) {
            return referer.replaceFirst("^https?://[^/]+", "");
        }
        return context.isEmpty() ? "/" : context + "/";
    }
}
