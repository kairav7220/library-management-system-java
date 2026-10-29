package com.library.lms.controller;

import com.library.lms.service.ChatHistoryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatHistoryServiceTest {

    private final ChatHistoryService history = new ChatHistoryService();

    @Test
    void recordsTurnsPerSession() {
        history.record("s1", "hi", "hello!", "Library Concierge");
        history.record("s1", "books?", "Try search.", "Library Concierge");
        history.record("s2", "yo", "hey", "Library Concierge");

        List<ChatHistoryService.Turn> turns = history.turns("s1");
        assertEquals(2, turns.size());
        assertEquals("hi", turns.get(0).userMessage());
        assertEquals("hello!", turns.get(0).agentResponse());
        assertEquals(1, history.turns("s2").size());
        assertEquals(0, history.turns("missing").size());
    }

    @Test
    void capsTurnsPerSession() {
        for (int i = 0; i < ChatHistoryService.MAX_TURNS_PER_SESSION + 20; i++) {
            history.record("s", "m" + i, "r" + i, "Library Concierge");
        }
        List<ChatHistoryService.Turn> turns = history.turns("s");
        assertEquals(ChatHistoryService.MAX_TURNS_PER_SESSION, turns.size());
        assertEquals("m" + (ChatHistoryService.MAX_TURNS_PER_SESSION + 19), turns.get(turns.size() - 1).userMessage());
    }

    @Test
    void listsSessionsNewestFirstWithTitles() {
        history.record("a", "first question ever", "ok", "Library Concierge");
        history.record("b", "second", "ok", "Library Concierge");

        List<ChatHistoryService.SessionSummary> sessions = history.listSessions();
        assertEquals(2, sessions.size());
        assertEquals("second", sessions.get(0).title());
        assertEquals("b", sessions.get(0).sessionId());
        assertTrue(sessions.get(0).lastActive().endsWith("Z") || sessions.get(0).lastActive().length() > 10);
    }

    @Test
    void deletesSession() {
        history.record("s", "hi", "hello", "Library Concierge");
        history.delete("s");
        assertEquals(0, history.turns("s").size());
        assertEquals(0, history.listSessions().size());
        history.delete("ghost");
    }

    @Test
    void ignoresBlankSessionIds() {
        history.record("", "hi", "hello", "Library Concierge");
        history.record(null, "hi", "hello", "Library Concierge");
        assertEquals(0, history.listSessions().size());
    }
}
