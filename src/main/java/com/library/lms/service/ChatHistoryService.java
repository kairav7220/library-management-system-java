package com.library.lms.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatHistoryService {

    public static final int MAX_TURNS_PER_SESSION = 50;
    public static final int MAX_SESSIONS = 100;

    public record Turn(
            @JsonProperty("user_message") String userMessage,
            @JsonProperty("agent_response") String agentResponse,
            @JsonProperty("agent_name") String agentName,
            @JsonProperty("timestamp") String timestamp) {
    }

    public record SessionSummary(
            @JsonProperty("session_id") String sessionId,
            @JsonProperty("title") String title,
            @JsonProperty("last_active") String lastActive) {
    }

    private record Session(String id, List<Turn> turns, String title, long lastActive, long seq) {
    }

    private final Map<String, Session> sessions = new LinkedHashMap<>();
    private long nextSeq = 0;

    public synchronized void record(String sessionId, String userMessage, String agentResponse, String agentName) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        Session session = sessions.get(sessionId);
        List<Turn> turns = session == null ? new ArrayList<>() : new ArrayList<>(session.turns());
        turns.add(new Turn(userMessage, agentResponse, agentName, Instant.now().toString()));
        if (turns.size() > MAX_TURNS_PER_SESSION) {
            turns.removeFirst();
        }
        String title = session == null || "New chat".equals(session.title())
                ? titleOf(userMessage) : session.title();
        long now = System.currentTimeMillis();
        sessions.put(sessionId, new Session(sessionId, turns, title, now, ++nextSeq));
        while (sessions.size() > MAX_SESSIONS) {
            String oldest = sessions.entrySet().stream()
                    .min(Comparator.comparingLong(e -> e.getValue().lastActive()))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            if (oldest == null) {
                break;
            }
            sessions.remove(oldest);
        }
    }

    public synchronized List<Turn> turns(String sessionId) {
        Session session = sessionId == null ? null : sessions.get(sessionId);
        return session == null ? List.of() : List.copyOf(session.turns());
    }

    public synchronized List<SessionSummary> listSessions() {
        return sessions.values().stream()
                .filter(session -> !session.turns().isEmpty())
                .sorted(Comparator.comparingLong(Session::seq).reversed())
                .map(session -> new SessionSummary(session.id(), session.title(), formatInstant(session.lastActive())))
                .toList();
    }

    public synchronized void delete(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }

    private String titleOf(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "New chat";
        }
        String trimmed = userMessage.trim();
        return trimmed.length() > 60 ? trimmed.substring(0, 60) : trimmed;
    }

    private String formatInstant(long epochMillis) {
        if (epochMillis <= 0) {
            return "";
        }
        return Instant.ofEpochMilli(epochMillis).toString();
    }
}
