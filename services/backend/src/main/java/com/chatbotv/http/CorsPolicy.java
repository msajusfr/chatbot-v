package com.chatbotv.http;

import com.sun.net.httpserver.HttpExchange;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

public final class CorsPolicy {

    private CorsPolicy() {
    }

    public static String resolveAllowedOrigin(HttpExchange exchange, String configuredOrigin) {
        String requestOrigin = exchange.getRequestHeaders().getFirst("Origin");
        if (requestOrigin == null || requestOrigin.isBlank()) {
            return configuredOrigin;
        }
        if (isAllowed(configuredOrigin, requestOrigin)) {
            return requestOrigin;
        }
        return configuredOrigin;
    }

    static boolean isAllowed(String configuredOrigin, String requestOrigin) {
        if (configuredOrigin == null || configuredOrigin.isBlank()) {
            return false;
        }

        return Arrays.stream(configuredOrigin.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .anyMatch(value -> matches(value, requestOrigin));
    }

    private static boolean matches(String configuredValue, String requestOrigin) {
        if ("*".equals(configuredValue)) {
            return true;
        }
        if (configuredValue.equalsIgnoreCase(requestOrigin)) {
            return true;
        }

        if (isLocalhost(configuredValue) && isLocalhost(requestOrigin)) {
            String configuredScheme = safeScheme(configuredValue);
            String requestScheme = safeScheme(requestOrigin);
            return configuredScheme != null && configuredScheme.equalsIgnoreCase(requestScheme);
        }

        return false;
    }

    private static boolean isLocalhost(String value) {
        try {
            String host = URI.create(value).getHost();
            if (host == null) {
                return false;
            }
            String lowerHost = host.toLowerCase(Locale.ROOT);
            return "localhost".equals(lowerHost) || "127.0.0.1".equals(lowerHost);
        } catch (Exception ex) {
            return false;
        }
    }

    private static String safeScheme(String value) {
        try {
            return URI.create(value).getScheme();
        } catch (Exception ex) {
            return null;
        }
    }
}
