package com.adria.account_service.config;

import com.adria.account_service.mcp.AccountMcpTools;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfig {

        @Bean
        ToolCallbackProvider accountTools(
                        AccountMcpTools tools) {
                return MethodToolCallbackProvider.builder()
                                .toolObjects(tools)
                                .build();
        }
}
