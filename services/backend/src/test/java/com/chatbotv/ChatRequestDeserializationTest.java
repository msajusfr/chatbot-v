package com.chatbotv;

import com.chatbotv.model.ChatModels.ChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void mapsIdFieldToChatIdForClientCompatibility() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String payload = """
                {
                  "id": "chat-from-client",
                  "message": "hello"
                }
                """;

        ChatRequest request = mapper.readValue(payload, ChatRequest.class);

        assertEquals("chat-from-client", request.chatId());
        assertEquals("hello", request.message());
    }

    @Test
    void extractsUserMessageFromVercelAiSdkPayload() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String payload = """
                {
                  "chatId": "chat-2",
                  "messages": [
                    { "role": "assistant", "content": "Hello" },
                    { "role": "user", "content": "show me a table" }
                  ]
                }
                """;

        ChatRequest request = mapper.readValue(payload, ChatRequest.class);

        assertEquals("chat-2", request.chatId());
        assertEquals("show me a table", request.message());
    }

    @Test
    void keepsMessageNullWhenPayloadContainsNoContent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String payload = """
                {
                  "chatId": "chat-3",
                  "messages": []
                }
                """;

        ChatRequest request = mapper.readValue(payload, ChatRequest.class);

        assertEquals("chat-3", request.chatId());
        assertNull(request.message());
    }
}
