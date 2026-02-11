package com.chatbotv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;
import java.util.List;

public class ChatModels {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatRequest(@JsonAlias("id") String chatId, String message) {}

    public record ChatResponse(String chatId, ChatbotVResponse response) {}

    public record ChatbotVResponse(String answerMarkdown, List<Artifact> artifacts, List<String> followUps, Meta meta) {}

    public record Artifact(String type, String title, String sql, SqlResult result, PresentationHints presentationHints) {}

    public record SqlResult(List<String> columns, List<List<Object>> rows, boolean isFictional, String notes) {}

    public record PresentationHints(String primary, String secondary, List<String> formatting) {}

    public record Meta(String modelName, double temperature, Instant generatedAt) {}
}
