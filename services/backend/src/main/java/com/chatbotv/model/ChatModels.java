package com.chatbotv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class ChatModels {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatRequest(String chatId, String message) {
        @JsonCreator
        public ChatRequest(
                @JsonProperty("chatId") @JsonAlias("id") String chatId,
                @JsonProperty("message") String message,
                @JsonProperty("messages") List<ClientMessage> messages
        ) {
            this(chatId, message != null ? message : extractLastUserMessage(messages));
        }

        private static String extractLastUserMessage(List<ClientMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return null;
            }
            for (int i = messages.size() - 1; i >= 0; i--) {
                ClientMessage candidate = messages.get(i);
                if (candidate != null && "user".equalsIgnoreCase(candidate.role())) {
                    String text = candidate.text();
                    if (text != null) {
                        return text;
                    }
                }
            }
            return messages.stream()
                    .filter(Objects::nonNull)
                    .map(ClientMessage::text)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClientMessage(String role, JsonNode content, List<MessagePart> parts) {
        private static final String PART_TYPE_TEXT = "text";

        public String text() {
            String fromContent = extractContentText(content);
            return fromContent != null ? fromContent : extractTextFromParts(parts);
        }

        private static String extractContentText(JsonNode node) {
            if (node == null || node.isNull()) {
                return null;
            }
            if (node.isTextual()) {
                String value = node.asText();
                return value.isBlank() ? null : value;
            }
            if (node.isArray()) {
                for (JsonNode item : node) {
                    String value = extractContentText(item);
                    if (value != null) {
                        return value;
                    }
                }
                return null;
            }
            if (node.isObject()) {
                JsonNode textNode = node.get("text");
                if (textNode != null && textNode.isTextual() && !textNode.asText().isBlank()) {
                    return textNode.asText();
                }
            }
            return null;
        }

        private static String extractTextFromParts(List<MessagePart> parts) {
            if (parts == null || parts.isEmpty()) {
                return null;
            }
            return parts.stream()
                    .filter(Objects::nonNull)
                    .filter(part -> PART_TYPE_TEXT.equalsIgnoreCase(part.type()))
                    .map(MessagePart::text)
                    .filter(Objects::nonNull)
                    .filter(value -> !value.isBlank())
                    .findFirst()
                    .orElse(null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessagePart(String type, String text) {}

    public record ChatResponse(String chatId, ChatbotVResponse response) {}

    public record ChatbotVResponse(String answerMarkdown, List<Artifact> artifacts, List<String> followUps, Meta meta) {}

    public record Artifact(String type, String title, String sql, SqlResult result, PresentationHints presentationHints) {}

    public record SqlResult(List<String> columns, List<List<Object>> rows, boolean isFictional, String notes) {}

    public record PresentationHints(String primary, String secondary, List<String> formatting) {}

    public record Meta(String modelName, double temperature, Instant generatedAt) {}
}
