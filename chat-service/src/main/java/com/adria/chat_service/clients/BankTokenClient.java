package com.adria.chat_service.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.discord.enabled", havingValue = "true")
public class BankTokenClient {

    private final RestClient restClient = RestClient.create();
    private final String tokenUri;
    private final String clientId;
    private final String username;
    private final String password;

    public BankTokenClient(
            @Value("${app.discord.token-uri}") String tokenUri,
            @Value("${app.discord.client-id}") String clientId,
            @Value("${app.discord.bank-username}") String username,
            @Value("${app.discord.bank-password}") String password) {
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    @SuppressWarnings("unchecked")
    public String fetchToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", username);
        form.add("password", password);

        Map<String, Object> response = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        return response == null ? null : (String) response.get("access_token");
    }
}
