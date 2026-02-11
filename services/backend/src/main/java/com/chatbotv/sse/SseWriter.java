package com.chatbotv.sse;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class SseWriter {
    private final OutputStream outputStream;
    private final ObjectMapper mapper;

    public SseWriter(OutputStream outputStream, ObjectMapper mapper) {
        this.outputStream = outputStream;
        this.mapper = mapper;
    }

    public synchronized void event(String event, Object payload) throws IOException {
        LinkedHashMap<String, Object> eventPayload = new LinkedHashMap<>();
        eventPayload.put("type", event);
        if (payload instanceof Map<?, ?> payloadMap) {
            for (Map.Entry<?, ?> entry : payloadMap.entrySet()) {
                eventPayload.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        } else if (payload != null) {
            eventPayload.put("data", payload);
        }

        String json = mapper.writeValueAsString(eventPayload);
        String chunk = "data: " + json + "\n\n";
        outputStream.write(chunk.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    public synchronized void done() throws IOException {
        outputStream.write("data: [DONE]\n\n".getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    public static Map<String, Object> map(Object... kv) {
        java.util.LinkedHashMap<String, Object> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }
}
