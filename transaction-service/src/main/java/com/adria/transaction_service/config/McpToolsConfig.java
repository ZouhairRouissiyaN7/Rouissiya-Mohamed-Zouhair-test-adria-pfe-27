package com.adria.transaction_service.config;

import com.adria.transaction_service.mcp.TransactionMcpTools;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfig {

        @Bean
        ToolCallbackProvider transactionTools(
                        TransactionMcpTools tools) {
                return MethodToolCallbackProvider.builder()
                                .toolObjects(tools)
                                .build();
        }
}
