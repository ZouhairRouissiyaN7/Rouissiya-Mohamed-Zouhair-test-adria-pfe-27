package com.adria.chat_service.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
public class McpToolCatalog {

    private static final String HIDDEN_PARAMETER = "bearerToken";

    private final McpProperties properties;

    public McpToolCatalog(McpProperties properties) {
        this.properties = properties;
    }

    public Session open() {
        return new Session(properties.servers());
    }

    public static class Session implements AutoCloseable {

        private static final Logger log = LoggerFactory.getLogger(Session.class);

        private final List<McpSyncClient> clients = new ArrayList<>();
        private final Map<String, McpSchema.Tool> tools = new LinkedHashMap<>();
        private final Map<String, McpSyncClient> owners = new HashMap<>();

        Session(List<McpProperties.Server> servers) {
            for (McpProperties.Server server : servers) {
                McpSyncClient client = null;

                try {
                    client = McpClient.sync(
                            HttpClientSseClientTransport.builder(server.url())
                                    .sseEndpoint(server.sseEndpoint())
                                    .build())
                            .requestTimeout(Duration.ofSeconds(30))
                            .initializationTimeout(Duration.ofSeconds(15))
                            .build();

                    client.initialize();

                    for (McpSchema.Tool tool : client.listTools().tools()) {
                        tools.put(tool.name(), tool);
                        owners.put(tool.name(), client);
                    }

                    clients.add(client);
                } catch (RuntimeException ex) {
                    log.warn("Serveur MCP {} indisponible : {}", server.name(), ex.toString());

                    closeQuietly(client);
                }
            }
        }

        public boolean hasTool(String name) {
            return tools.containsKey(name);
        }

        public String describe() {
            StringBuilder description = new StringBuilder();

            for (McpSchema.Tool tool : tools.values()) {
                description.append("- ")
                        .append(tool.name())
                        .append("(")
                        .append(parameters(tool))
                        .append(") : ")
                        .append(tool.description())
                        .append("\n");
            }

            return description.toString();
        }

        public String call(
                String name,
                Map<String, Object> arguments,
                String bearerToken) {
            McpSyncClient client = owners.get(name);

            if (client == null) {
                return "ERREUR : outil inconnu " + name;
            }

            Map<String, Object> enriched = new HashMap<>(arguments);

            enriched.put(HIDDEN_PARAMETER, bearerToken);

            try {
                McpSchema.CallToolResult result = client.callTool(
                        new McpSchema.CallToolRequest(name, enriched));

                StringBuilder text = new StringBuilder();

                for (McpSchema.Content content : result.content()) {
                    if (content instanceof McpSchema.TextContent textContent) {
                        text.append(textContent.text());
                    }
                }

                return Boolean.TRUE.equals(result.isError())
                        ? "ERREUR : " + text
                        : text.toString();
            } catch (RuntimeException ex) {
                log.warn("Appel de l'outil {} impossible : {}", name, ex.toString());

                return "ERREUR : l'outil " + name + " est temporairement indisponible";
            }
        }

        @Override
        public void close() {
            clients.forEach(Session::closeQuietly);
        }

        private static void closeQuietly(McpSyncClient client) {
            if (client == null) {
                return;
            }

            try {
                client.closeGracefully();
            } catch (RuntimeException ignored) {
            }
        }

        @SuppressWarnings("unchecked")
        private String parameters(McpSchema.Tool tool) {
            Map<String, Object> properties = tool.inputSchema() == null
                    ? null
                    : tool.inputSchema().properties();

            if (properties == null) {
                return "";
            }

            List<String> described = new ArrayList<>();

            properties.forEach((name, definition) -> {
                if (name.equals(HIDDEN_PARAMETER)) {
                    return;
                }

                Map<String, Object> details = (Map<String, Object>) definition;

                described.add(name + ": " + details.getOrDefault("type", "string")
                        + " (" + details.getOrDefault("description", "") + ")");
            });

            return String.join(", ", described);
        }
    }
}
