package com.library.lms.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;
import com.library.lms.ai.ChatContext;
import com.library.lms.ai.LibraryAssistant;
import com.library.lms.entity.User;
import com.library.lms.service.ChatHistoryService;
import com.library.lms.service.MemberService;
import com.library.lms.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private static final String AGENT_NAME = "Library Concierge";

    private final LibraryAssistant assistant;
    private final ChatHistoryService chatHistory;
    private final UserService userService;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ChatController(LibraryAssistant assistant, ChatHistoryService chatHistory,
                          UserService userService, MemberService memberService, ObjectMapper objectMapper) {
        this.assistant = assistant;
        this.chatHistory = chatHistory;
        this.userService = userService;
        this.memberService = memberService;
        this.objectMapper = objectMapper;
    }

    public record ChatRequest(@JsonProperty("message") String message,
                              @JsonProperty("session_id") String sessionId) {
    }

    private record SsePayload(String type, String text, String response, String agent, String message) {
        static SsePayload delta(String text) {
            return new SsePayload("delta", text, null, null, null);
        }

        static SsePayload done(String response) {
            return new SsePayload("done", null, response, AGENT_NAME, null);
        }

        static SsePayload error(String message) {
            return new SsePayload("error", null, null, null, message);
        }
    }

    public record HistoryResponse(@JsonProperty("session_id") String sessionId,
                                  @JsonProperty("turns") List<ChatHistoryService.Turn> turns) {
    }

    public record SessionsResponse(@JsonProperty("sessions") List<ChatHistoryService.SessionSummary> sessions) {
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody ChatRequest request, jakarta.servlet.http.HttpServletResponse httpResponse) {
        httpResponse.setHeader("Cache-Control", "no-cache");
        httpResponse.setHeader("X-Accel-Buffering", "no");

        String message = request.message() == null ? "" : request.message().trim();
        String sessionId = (request.sessionId() == null || request.sessionId().isBlank())
                ? "default" : request.sessionId().trim();

        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException ignored) {
        }

        String memberRef = resolveMemberRef();
        executor.execute(() -> {
            ChatContext.setMemberRef(memberRef);
            StringBuilder full = new StringBuilder();
            try {
                assistant.chat(sessionId, message)
                        .onPartialResponse(token -> {
                            full.append(token);
                            send(emitter, SsePayload.delta(token));
                        })
                        .onCompleteResponse(response -> {
                            String answer = response == null || response.aiMessage() == null
                                    || response.aiMessage().text() == null ? full.toString() : response.aiMessage().text();
                            chatHistory.record(sessionId, message, answer, AGENT_NAME);
                            send(emitter, SsePayload.done(answer));
                            emitter.complete();
                        })
                        .onError(error -> {
                            send(emitter, SsePayload.error(friendlyError(error)));
                            emitter.complete();
                        })
                        .start();
            } catch (RuntimeException e) {
                send(emitter, SsePayload.error(friendlyError(e)));
                emitter.complete();
            } finally {
                ChatContext.clear();
            }
        });
        return emitter;
    }

    private void send(SseEmitter emitter, SsePayload payload) {
        try {
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(payload)));
        } catch (IOException | IllegalStateException e) {
            emitter.complete();
        }
    }

    private String friendlyError(Throwable error) {
        log.error("Chat stream failed", error);
        String raw = error.getMessage() == null ? "" : error.getMessage();
        if (raw.contains("401")) {
            return "AI service authentication failed — check MISTRAL_API_KEY.";
        } else if (raw.contains("403")) {
            return "Mistral API denied the request — the API key may be invalid.";
        } else if (raw.contains("429")) {
            return "The AI service is rate limited — try again in a moment.";
        } else if (raw.contains("Connection refused") || raw.contains("timeout")) {
            return "Could not reach the AI service — check network connectivity.";
        }
        return "The concierge hit an unexpected error — please try again.";
    }

    private String resolveMemberRef() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return userService.getByUsername(auth.getName())
                .filter(u -> "Member".equalsIgnoreCase(u.getUserType()))
                .map(User::getUserId)
                .map(memberService::memRefForUserId)
                .orElse(null);
    }

    @GetMapping("/chat/history")
    public HistoryResponse history(@RequestParam(name = "session_id", required = false) String sessionId) {
        String id = sessionId == null || sessionId.isBlank() ? "default" : sessionId;
        return new HistoryResponse(id, chatHistory.turns(id));
    }

    @GetMapping("/chat/sessions")
    public SessionsResponse sessions() {
        return new SessionsResponse(chatHistory.listSessions());
    }

    @DeleteMapping("/chat/sessions/{sessionId}")
    public Map<String, Boolean> delete(@PathVariable String sessionId) {
        chatHistory.delete(sessionId);
        return Map.of("deleted", true);
    }
}
