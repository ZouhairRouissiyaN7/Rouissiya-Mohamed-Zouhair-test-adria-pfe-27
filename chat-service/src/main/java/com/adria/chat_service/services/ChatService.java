package com.adria.chat_service.services;

import com.adria.chat_service.dtos.ChatResponse;
import com.adria.chat_service.mcp.McpToolCatalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            Tu es l'assistant virtuel de la banque ADRIA. Tu automatises les opérations bancaires
            de l'utilisateur en utilisant les outils suivants :

            %s
            Tu dois répondre UNIQUEMENT avec un objet JSON valide, sans aucun texte autour,
            sous l'une des deux formes suivantes :

            {"action":"tool","tool":"nom_de_l_outil","arguments":{"parametre":"valeur"}}
            {"action":"answer","message":"ta réponse en français"}

            Exemples (les identifiants ci-dessous sont fictifs) :

            Utilisateur : Quel est le solde du compte 11111111-1111-1111-1111-111111111111 ?
            Toi : {"action":"tool","tool":"getBalance","arguments":{"accountId":"11111111-1111-1111-1111-111111111111"}}
            Utilisateur : Résultat de l'outil getBalance : {"accountId":"11111111-1111-1111-1111-111111111111","currency":"EUR","balance":120.5}
            Toi : {"action":"answer","message":"Le solde de votre compte est de 120,50 EUR."}

            Utilisateur : Vire 30 EUR du compte 11111111-1111-1111-1111-111111111111 vers le compte 22222222-2222-2222-2222-222222222222
            Toi : {"action":"tool","tool":"transfer","arguments":{"sourceAccountId":"11111111-1111-1111-1111-111111111111","destinationAccountId":"22222222-2222-2222-2222-222222222222","amount":30}}

            Règles importantes :
            - Tu ne connais AUCUN solde ni aucune transaction : pour toute information sur un compte, un solde,
              une transaction ou un virement, tu dois TOUJOURS appeler l'outil correspondant avant de répondre.
            - Utilise un outil dès que la demande concerne un compte, un solde, une transaction ou un virement.
            - Les identifiants de compte sont des UUID : ne les invente jamais, demande-les à l'utilisateur s'ils manquent.
            - Appelle un seul outil à la fois, puis attends son résultat.
            - Quand tu as le résultat d'un outil, réponds avec l'action answer en le résumant clairement.
            - Si un outil renvoie une erreur, explique-la simplement à l'utilisateur.
            - Ne demande jamais de mot de passe ni de jeton d'authentification.
            - Réponds toujours en français, de façon concise.
            """;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final McpToolCatalog catalog;
    private final ObjectMapper mapper;
    private final int maxSteps;

    public ChatService(
            ChatClient.Builder builder,
            McpToolCatalog catalog,
            ObjectMapper mapper,
            @Value("${app.agent.max-steps:5}") int maxSteps) {
        this.chatClient = builder.build();
        this.catalog = catalog;
        this.mapper = mapper;
        this.maxSteps = maxSteps;
    }

    public ChatResponse chat(
            String message,
            String bearerToken) {

        String system = SYSTEM_PROMPT.formatted(catalog.describe());

        List<Message> messages = new ArrayList<>();

        messages.add(new UserMessage(message));

        StringBuilder known = new StringBuilder(message);

        String lastReply = null;

        for (int step = 0; step < maxSteps; step++) {

            lastReply = chatClient.prompt()
                    .system(system)
                    .messages(messages)
                    .call()
                    .content();

            JsonNode node = parse(lastReply);

            if (node == null || !"tool".equals(node.path("action").asText())) {
                log.info("Étape {} : réponse finale", step + 1);

                return new ChatResponse(answer(node, lastReply));
            }

            String tool = node.path("tool").asText();

            log.info("Étape {} : appel de l'outil {} avec {}", step + 1, tool, node.path("arguments"));

            Map<String, Object> arguments = arguments(node);

            String unknownIdentifier = unknownIdentifier(arguments, known);

            String result;

            if (!catalog.hasTool(tool)) {
                result = "ERREUR : outil inconnu " + tool;
            } else if (unknownIdentifier != null) {
                result = "ERREUR : l'identifiant " + unknownIdentifier
                        + " ne figure pas dans la conversation. Recopie exactement l'identifiant fourni par l'utilisateur.";
            } else {
                result = catalog.call(tool, arguments, bearerToken);
            }

            known.append('\n').append(result);

            log.info("Étape {} : résultat de {} = {}", step + 1, tool, result);

            String instruction = unknownIdentifier != null
                    ? "\nRecopie l'identifiant EXACTEMENT comme dans le message de l'utilisateur et rappelle l'outil avec l'action tool."
                    : "\nRéponds maintenant avec l'action answer, ou appelle un autre outil si nécessaire.";

            messages.add(new AssistantMessage(lastReply));
            messages.add(new UserMessage(
                    "Résultat de l'outil " + tool + " : " + result + instruction));
        }

        return new ChatResponse(
                "Je n'ai pas réussi à terminer cette opération. Veuillez reformuler votre demande.");
    }

    private JsonNode parse(String reply) {
        if (reply == null || reply.isBlank()) {
            return null;
        }

        try {
            return mapper.readTree(reply);
        } catch (Exception ex) {
            int start = reply.indexOf('{');
            int end = reply.lastIndexOf('}');

            if (start < 0 || end <= start) {
                return null;
            }

            try {
                return mapper.readTree(reply.substring(start, end + 1));
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String unknownIdentifier(
            Map<String, Object> arguments,
            StringBuilder known) {
        Set<String> tokens = new HashSet<>(
                Arrays.asList(known.toString().split("[^A-Za-z0-9-]+")));

        for (Map.Entry<String, Object> argument : arguments.entrySet()) {
            if (argument.getKey().endsWith("Id")
                    && argument.getValue() instanceof String value
                    && !tokens.contains(value)) {
                return value;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> arguments(JsonNode node) {
        JsonNode arguments = node.path("arguments");

        return arguments.isObject()
                ? mapper.convertValue(arguments, Map.class)
                : Map.of();
    }

    private String answer(
            JsonNode node,
            String reply) {
        if (node != null && node.hasNonNull("message")) {
            return node.get("message").asText();
        }

        if (reply == null || reply.isBlank()) {
            return "Je n'ai pas pu générer de réponse. Veuillez réessayer.";
        }

        return reply;
    }
}
