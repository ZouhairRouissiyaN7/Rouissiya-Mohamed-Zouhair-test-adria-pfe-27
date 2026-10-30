package com.adria.chat_service.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class McpToolCatalog {

    private static final Logger log = LoggerFactory.getLogger(McpToolCatalog.class);

    private static final String HIDDEN_PARAMETER = "bearerToken";

    private final List<McpSyncClient> clients;
    private final Map<String, McpSchema.Tool> tools = new LinkedHashMap<>();
    private final Map<String, McpSyncClient> owners = new HashMap<>();
    private final Set<McpSyncClient> initialized = new HashSet<>();

    public McpToolCatalog(List<McpSyncClient> clients) {
        this.clients = clients;
    }

    public synchronized boolean hasTool(String name) {
        refreshIfEmpty();

        return tools.containsKey(name);
    }

    public synchronized String describe() {
        refreshIfEmpty();

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

    public synchronized String call(
            String name,
            Map<String, Object> arguments,
            String bearerToken) {
        refreshIfEmpty();

        McpSyncClient client = owners.get(name);

        if (client == null) {
            return "ERREUR : outil inconnu " + name;
        }

        Map<String, Object> enriched = new HashMap<>(arguments);

        enriched.put(HIDDEN_PARAMETER, bearerToken);

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
    }

    private void refreshIfEmpty() {
        if (!tools.isEmpty()) {
            return;
        }

        for (McpSyncClient client : clients) {
            try {
                if (initialized.add(client)) {
                    client.initialize();
                }

                for (McpSchema.Tool tool : client.listTools().tools()) {
                    tools.put(tool.name(), tool);
                    owners.put(tool.name(), client);
                }
            } catch (RuntimeException ex) {
                initialized.remove(client);

                log.warn("Serveur MCP indisponible : {}", ex.toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String parameters(McpSchema.Tool tool) {
        Map<String, Object> properties = tool.inputSchema() == null
                ? Map.of()
                : tool.inputSchema().properties();

        List<String> described = new ArrayList<>();

        if (properties == null) {
            return "";
        }

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
