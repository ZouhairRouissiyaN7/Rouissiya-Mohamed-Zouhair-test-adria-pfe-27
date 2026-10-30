package com.adria.chat_service.mcp;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.mcp")
public record McpProperties(
        List<Server> servers) {

    public record Server(
            String name,
            String url,
            String sseEndpoint) {
    }
}
