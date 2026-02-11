package com.chatbotv.chat;

import com.chatbotv.model.ChatModels.ChatbotVResponse;

public interface ChatAssistantService {
    ChatbotVResponse generate(String userMessage) throws Exception;
}
