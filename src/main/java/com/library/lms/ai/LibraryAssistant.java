package com.library.lms.ai;

import com.library.lms.config.LangChain4jConfig;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface LibraryAssistant {

    String SYSTEM_PROMPT = LangChain4jConfig.LUMINA_PERSONA;

    @SystemMessage(SYSTEM_PROMPT)
    TokenStream chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
