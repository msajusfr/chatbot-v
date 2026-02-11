package com.chatbotv;

import com.chatbotv.sse.SseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SseWriterTest {

    @Test
    void writesExpectedSseContractFragments() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SseWriter writer = new SseWriter(out, new ObjectMapper());

        writer.event("start", SseWriter.map("chatId", "c1"));
        writer.event("text-delta", SseWriter.map("delta", "hello"));
        writer.done();

        String payload = out.toString();
        assertTrue(payload.contains("event: start"));
        assertTrue(payload.contains("event: text-delta"));
        assertTrue(payload.contains("data: [DONE]"));
    }
}
