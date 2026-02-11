package com.chatbotv;

import com.chatbotv.model.ChatModels.ChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatRequestDeserializationTest {

    @Test
    void ignoresUnexpectedFieldsInRequestPayload() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String payload = """
                {
                  "id": "msg-1",
                  "chatId": "chat-1",
                  "message": "hello"
                }
                """;

        ChatRequest request = mapper.readValue(payload, ChatRequest.class);

        assertEquals("chat-1", request.chatId());
        assertEquals("hello", request.message());
    }
}
