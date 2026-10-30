package com.adria.chat_service.discord;

import jakarta.annotation.PreDestroy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@ConditionalOnProperty(name = "app.discord.enabled", havingValue = "true")
public class DiscordBotManager {

    private static final Logger log = LoggerFactory.getLogger(DiscordBotManager.class);

    private final DiscordCommandListener listener;
    private final String token;
    private final String guildId;

    private JDA jda;

    public DiscordBotManager(
            DiscordCommandListener listener,
            @Value("${app.discord.token}") String token,
            @Value("${app.discord.guild-id}") String guildId) {
        this.listener = listener;
        this.token = token;
        this.guildId = guildId;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startBot() throws InterruptedException {

        if (token.isBlank() || guildId.isBlank()) {
            throw new IllegalStateException(
                    "Discord est activé mais le jeton ou l'identifiant du serveur est manquant");
        }

        jda = JDABuilder.createLight(
                token,
                Collections.emptyList())
                .addEventListeners(listener)
                .build();

        jda.awaitReady();

        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new IllegalStateException(
                    "Le bot Discord n'est pas membre du serveur " + guildId);
        }

        guild.upsertCommand(
                Commands.slash(
                        "ask",
                        "Posez une question à l'assistant bancaire ADRIA")
                        .addOption(
                                OptionType.STRING,
                                "question",
                                "Votre demande bancaire",
                                true))
                .queue(
                        command -> log.info("Commande Discord /ask enregistrée"),
                        error -> log.error(
                                "Échec de l'enregistrement de la commande Discord : {}",
                                error.getMessage()));

        log.info("Bot Discord connecté : {}", jda.getSelfUser().getName());
    }

    @PreDestroy
    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
        }
    }
}
