package com.chatbotv;

import com.chatbotv.http.CorsPolicy;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CorsPolicyTest {

    @Test
    void resolvesExactOrigin() {
        HttpExchange exchange = exchangeWithOrigin("http://localhost:3000");

        String allowed = CorsPolicy.resolveAllowedOrigin(exchange, "http://localhost:3000");

        assertEquals("http://localhost:3000", allowed);
    }

    @Test
    void allowsAnyLocalhostPortWhenConfiguredForLocalhost() {
        HttpExchange exchange = exchangeWithOrigin("http://localhost:5173");

        String allowed = CorsPolicy.resolveAllowedOrigin(exchange, "http://localhost:3000");

        assertEquals("http://localhost:5173", allowed);
    }

    @Test
    void fallsBackToConfiguredOriginForUnknownSite() {
        HttpExchange exchange = exchangeWithOrigin("https://evil.example");

        String allowed = CorsPolicy.resolveAllowedOrigin(exchange, "http://localhost:3000");

        assertEquals("http://localhost:3000", allowed);
    }

    @Test
    void acceptsMultipleConfiguredOrigins() {
        HttpExchange exchange = exchangeWithOrigin("https://app.example.com");

        String allowed = CorsPolicy.resolveAllowedOrigin(exchange, "http://localhost:3000, https://app.example.com");

        assertEquals("https://app.example.com", allowed);
    }

    private static HttpExchange exchangeWithOrigin(String origin) {
        return new FakeExchange(origin);
    }

    private static final class FakeExchange extends HttpExchange {
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();

        private FakeExchange(String origin) {
            requestHeaders.add("Origin", origin);
        }

        @Override
        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public URI getRequestURI() {
            return URI.create("/");
        }

        @Override
        public String getRequestMethod() {
            return "GET";
        }

        @Override
        public HttpContext getHttpContext() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public InputStream getRequestBody() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public OutputStream getResponseBody() {
            return new ByteArrayOutputStream();
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) {
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return new InetSocketAddress("127.0.0.1", 1234);
        }

        @Override
        public int getResponseCode() {
            return 200;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return new InetSocketAddress("127.0.0.1", 8080);
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {
        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {
        }

        @Override
        public HttpPrincipal getPrincipal() {
            return null;
        }
    }
}
