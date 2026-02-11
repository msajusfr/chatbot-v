package com.chatbotv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
            this.chatId = chatId;
            this.message = message != null ? message : extractLastUserMessage(messages);
        }

        private static String extractLastUserMessage(List<ClientMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return null;
            }
            for (int i = messages.size() - 1; i >= 0; i--) {
                ClientMessage candidate = messages.get(i);
                if (candidate != null && "user".equalsIgnoreCase(candidate.role()) && candidate.content() != null) {
                    return candidate.content();
                }
            }
            return messages.stream()
                    .filter(Objects::nonNull)
                    .map(ClientMessage::content)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClientMessage(String role, String content) {}

    public record ChatResponse(String chatId, ChatbotVResponse response) {}

    public record ChatbotVResponse(String answerMarkdown, List<Artifact> artifacts, List<String> followUps, Meta meta) {}

    public record Artifact(String type, String title, String sql, SqlResult result, PresentationHints presentationHints) {}

    public record SqlResult(List<String> columns, List<List<Object>> rows, boolean isFictional, String notes) {}

    public record PresentationHints(String primary, String secondary, List<String> formatting) {}

    public record Meta(String modelName, double temperature, Instant generatedAt) {}
}
