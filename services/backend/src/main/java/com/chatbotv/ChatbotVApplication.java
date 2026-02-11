package com.chatbotv;

import com.chatbotv.chat.ChatAssistantService;
import com.chatbotv.chat.LangChain4jChatAssistantService;
import com.chatbotv.http.HttpUtils;
import com.chatbotv.model.ChatModels.ChatRequest;
import com.chatbotv.model.ChatModels.ChatResponse;
import com.chatbotv.model.ChatModels.ChatbotVResponse;
import com.chatbotv.security.TokenBucketRateLimiter;
import com.chatbotv.sse.SseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class ChatbotVApplication {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        int port = Integer.parseInt(env(dotenv, "BACKEND_PORT", "8080"));
        String allowedOrigin = env(dotenv, "FRONTEND_ORIGIN", "http://localhost:3000");
        String token = env(dotenv, "CHATBOTV_INTERNAL_TOKEN", "");
        String model = env(dotenv, "MODEL_NAME", "gpt-4o-mini");
        double temp = Double.parseDouble(env(dotenv, "TEMPERATURE", "0.2"));
        int timeoutSecs = Integer.parseInt(env(dotenv, "TIMEOUT_SECS", "45"));
        String openAiKey = env(dotenv, "OPENAI_API_KEY", "");

        ChatAssistantService assistantService = new LangChain4jChatAssistantService(openAiKey, model, temp, timeoutSecs, MAPPER);
        Map<String, List<ChatbotVResponse>> inMemoryChats = new ConcurrentHashMap<>();
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(20, 5);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", withCommon(exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpUtils.status(exchange, 405, "Method not allowed");
                return;
            }
            HttpUtils.json(exchange, MAPPER, 200, Map.of(
                    "name", "chatbot-v-backend",
                    "status", "ok",
                    "health", "/healthz",
                    "endpoints", List.of("POST /api/v1/chat", "POST /api/v1/chat/stream", "DELETE /api/v1/chats/{chatId}")
            ));
        }, allowedOrigin, token, rateLimiter));
        server.createContext("/healthz", withCommon(exchange -> HttpUtils.json(exchange, MAPPER, 200, Map.of("status", "ok")), allowedOrigin, token, rateLimiter));

        server.createContext("/api/v1/chat", withCommon(exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpUtils.status(exchange, 405, "Method not allowed");
                return;
            }
            ChatRequest req = MAPPER.readValue(HttpUtils.readBody(exchange), ChatRequest.class);
            String chatId = req.chatId() == null || req.chatId().isBlank() ? UUID.randomUUID().toString() : req.chatId();
            ChatbotVResponse response = assistantService.generate(req.message());
            inMemoryChats.computeIfAbsent(chatId, x -> new java.util.ArrayList<>()).add(response);
            HttpUtils.json(exchange, MAPPER, 200, new ChatResponse(chatId, response));
        }, allowedOrigin, token, rateLimiter));

        server.createContext("/api/v1/chat/stream", withCommon(exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpUtils.status(exchange, 405, "Method not allowed");
                return;
            }
            ChatRequest req = MAPPER.readValue(HttpUtils.readBody(exchange), ChatRequest.class);
            String chatId = req.chatId() == null || req.chatId().isBlank() ? UUID.randomUUID().toString() : req.chatId();
            ChatbotVResponse response = assistantService.generate(req.message());
            inMemoryChats.computeIfAbsent(chatId, x -> new java.util.ArrayList<>()).add(response);

            exchange.getResponseHeaders().add("Content-Type", "text/event-stream; charset=utf-8");
            exchange.getResponseHeaders().add("Cache-Control", "no-cache");
            exchange.getResponseHeaders().add("Connection", "keep-alive");
            exchange.getResponseHeaders().add("x-vercel-ai-ui-message-stream", "v1");
            exchange.sendResponseHeaders(200, 0);

            SseWriter writer = new SseWriter(exchange.getResponseBody(), MAPPER);
            writer.event("start", SseWriter.map("chatId", chatId));
            writer.event("text-start", SseWriter.map("id", "assistant-text-1"));
            writer.event("text-delta", SseWriter.map("id", "assistant-text-1", "delta", response.answerMarkdown()));
            writer.event("text-end", SseWriter.map("id", "assistant-text-1"));
            if (!response.artifacts().isEmpty()) {
                writer.event("data-sql", SseWriter.map("type", "data-sql", "data", response.artifacts().get(0)));
            }
            writer.event("finish", SseWriter.map("followUps", response.followUps(), "meta", response.meta()));
            writer.done();
            exchange.close();
        }, allowedOrigin, token, rateLimiter));

        server.createContext("/api/v1/chats", withCommon(exchange -> {
            if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                HttpUtils.status(exchange, 405, "Method not allowed");
                return;
            }
            String[] parts = exchange.getRequestURI().getPath().split("/");
            String chatId = parts[parts.length - 1];
            inMemoryChats.remove(chatId);
            HttpUtils.json(exchange, MAPPER, 200, Map.of("deleted", chatId));
        }, allowedOrigin, token, rateLimiter));

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("chatbot-v backend running on http://localhost:" + port);
    }

    private static HttpHandler withCommon(HttpHandler delegate, String allowedOrigin, String token, TokenBucketRateLimiter rateLimiter) {
        return exchange -> {
            try {
                cors(exchange, allowedOrigin);
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
                if (!rateLimiter.allow(ip)) {
                    HttpUtils.status(exchange, 429, "Too Many Requests");
                    return;
                }
                if (!token.isBlank()) {
                    String auth = exchange.getRequestHeaders().getFirst("Authorization");
                    String expected = "Bearer " + token;
                    if (auth == null || !auth.equals(expected)) {
                        HttpUtils.status(exchange, 401, "Unauthorized");
                        return;
                    }
                }
                delegate.handle(exchange);
            } catch (Exception ex) {
                ex.printStackTrace();
                if (exchange.getResponseBody() != null) {
                    HttpUtils.status(exchange, 500, "Internal server error");
                }
            }
        };
    }

    private static void cors(HttpExchange exchange, String allowedOrigin) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Authorization, Content-Type");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
    }

    private static String env(Dotenv dotenv, String key, String fallback) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
