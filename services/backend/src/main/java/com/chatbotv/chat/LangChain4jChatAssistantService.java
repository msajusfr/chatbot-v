package com.chatbotv.chat;

import com.chatbotv.model.ChatModels.Artifact;
import com.chatbotv.model.ChatModels.ChatbotVResponse;
import com.chatbotv.model.ChatModels.Meta;
import com.chatbotv.model.ChatModels.PresentationHints;
import com.chatbotv.model.ChatModels.SqlResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class LangChain4jChatAssistantService implements ChatAssistantService {
    private final String modelName;
    private final double temperature;
    private final ObjectMapper mapper;
    private final OpenAiChatModel model;

    public LangChain4jChatAssistantService(String apiKey, String modelName, double temperature, int timeoutSecs, ObjectMapper mapper) {
        this.modelName = modelName;
        this.temperature = temperature;
        this.mapper = mapper;
        this.model = apiKey == null || apiKey.isBlank() ? null : OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeoutSecs))
                .build();
    }

    @Override
    public ChatbotVResponse generate(String userMessage) throws Exception {
        String safeUserMessage = userMessage == null ? "" : userMessage;
        if (model == null) return fake(userMessage);

        String schemaPrompt = """
                Réponds strictement en JSON valide (sans markdown) selon ce schéma:
                {
                  \"answerMarkdown\": \"string avec disclaimer explicite que les données sont fictives\",
                  \"artifacts\": [{
                    \"type\": \"sql.query\",
                    \"title\": \"string\",
                    \"sql\": \"string\",
                    \"result\": {\"columns\": [\"string\"], \"rows\": [[\"any\"]], \"isFictional\": true, \"notes\": \"string\"},
                    \"presentationHints\": {\"primary\": \"bar|scatter|line|kpi|table\", \"secondary\": \"...\", \"formatting\": [\"...\"]}
                  }],
                  \"followUps\": [\"string\"],
                  \"meta\": {\"modelName\": \"%s\", \"temperature\": %s, \"generatedAt\": \"ISO-8601\"}
                }
                isFictional DOIT toujours être true.
                """.formatted(modelName, temperature);

        String prompt = schemaPrompt + "\nQuestion utilisateur: " + safeUserMessage;
        Exception parseError = null;
        for (int i = 0; i < 3; i++) {
            String text = model.chat(prompt);
            try {
                ChatbotVResponse parsed = mapper.readValue(text, ChatbotVResponse.class);
                return enforce(parsed);
            } catch (Exception ex) {
                parseError = ex;
                prompt = "JSON invalide, corrige strictement. Dernière sortie: " + text;
            }
        }
        throw parseError;
    }

    private ChatbotVResponse enforce(ChatbotVResponse input) {
        if (input.artifacts() == null || input.artifacts().isEmpty()) {
            return fake("fallback");
        }
        Artifact a = input.artifacts().get(0);
        Artifact fixed = new Artifact("sql.query", a.title(), a.sql(),
                new SqlResult(a.result().columns(), a.result().rows(), true, a.result().notes()), a.presentationHints());
        String md = input.answerMarkdown().contains("fict") ? input.answerMarkdown() :
                input.answerMarkdown() + "\n\n> ⚠️ Les données présentées sont fictives.";
        return new ChatbotVResponse(md, List.of(fixed), input.followUps(),
                new Meta(modelName, temperature, Instant.now()));
    }

    private ChatbotVResponse fake(String userMessage) {
        String lower = userMessage == null ? "" : userMessage.toLowerCase();
        String primary = lower.contains("pib") || lower.contains("population") ? "scatter" : "bar";
        Artifact artifact = new Artifact(
                "sql.query",
                "Résultats fictifs",
                "SELECT dimension, metric FROM analytics_demo ORDER BY metric DESC LIMIT 5;",
                new SqlResult(List.of("dimension", "metric"), List.of(List.of("A", 1200), List.of("B", 980), List.of("C", 870)), true,
                        "Dataset in-memory de démonstration"),
                new PresentationHints(primary, "table", List.of("compact", "currency"))
        );
        return new ChatbotVResponse(
                "Voici une réponse basée sur des données fictives pour illustrer l'analyse SQL.\n\n> ⚠️ Données fictives uniquement.",
                List.of(artifact),
                List.of("Veux-tu une vue KPI ?", "Souhaites-tu un tri différent ?"),
                new Meta(modelName, temperature, Instant.now())
        );
    }
}
