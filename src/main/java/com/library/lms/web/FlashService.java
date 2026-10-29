package com.library.lms.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FlashService {

    public static final String SESSION_KEY = "FLASH_MESSAGES";

    public record FlashMessage(String category, String message) {
    }

    public void add(String category, String message) {
        add(null, category, message);
    }

    public void add(HttpServletRequest request, String category, String message) {
        if (request == null) {
            return;
        }
        HttpSession session = request.getSession(true);
        List<FlashMessage> messages = read(session);
        messages.add(new FlashMessage(category, message));
        session.setAttribute(SESSION_KEY, messages);
    }

    @SuppressWarnings("unchecked")
    public List<FlashMessage> drain(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return List.of();
        }
        Object value = session.getAttribute(SESSION_KEY);
        session.removeAttribute(SESSION_KEY);
        if (value instanceof List<?> list) {
            return new ArrayList<>((List<FlashMessage>) list);
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<FlashMessage> read(HttpSession session) {
        Object value = session.getAttribute(SESSION_KEY);
        if (value instanceof List<?>) {
            return new ArrayList<>((List<FlashMessage>) value);
        }
        return new ArrayList<>();
    }
}
