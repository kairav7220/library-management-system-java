package com.library.lms.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"Admin"})
class ChatEndpointsTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void historyReturnsSessionIdAndTurns() throws Exception {
        mvc.perform(get("/chat/history").param("session_id", "test-sess"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session_id").value("test-sess"))
                .andExpect(jsonPath("$.turns").isArray());
    }

    @Test
    void sessionsListIsWrappedInSessionsKey() throws Exception {
        mvc.perform(get("/chat/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessions").isArray());
    }

    @Test
    void deleteSessionRespondsDeletedTrue() throws Exception {
        mvc.perform(delete("/chat/sessions/test-sess").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }
}
