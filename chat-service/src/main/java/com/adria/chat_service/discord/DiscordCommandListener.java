package com.adria.chat_service.discord;

import com.adria.chat_service.clients.BankTokenClient;
import com.adria.chat_service.services.ChatService;

import jakarta.annotation.PreDestroy;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ConditionalOnProperty(name = "app.discord.enabled", havingValue = "true")
public class DiscordCommandListener extends ListenerAdapter {

    private final ChatService chatService;
    private final BankTokenClient tokenClient;
    private final String guildId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DiscordCommandListener(
            ChatService chatService,
            BankTokenClient tokenClient,
            @Value("${app.discord.guild-id}") String guildId) {
        this.chatService = chatService;
        this.tokenClient = tokenClient;
        this.guildId = guildId;
    }

    @Override
    public void onSlashCommandInteraction(
            SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ask")) {
            return;
        }

        if (event.getGuild() == null ||
                !event.getGuild().getId().equals(guildId)) {
            event.reply(
                    "Cette commande est uniquement disponible sur le serveur de test configuré.").setEphemeral(true).queue();
            return;
        }

        String question = event.getOption(
                "question",
                OptionMapping::getAsString);

        if (question == null || question.isBlank()) {
            event.reply("Veuillez saisir une question.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (question.length() > 1000) {
            event.reply("Les questions doivent faire moins de 1000 caractères.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply(true).queue(hook -> {

            executor.execute(() -> {
                String reply;

                try {
                    reply = chatService.chat(question, tokenClient.fetchToken()).reply();

                    if (reply == null || reply.isBlank()) {
                        reply = "Je n'ai pas pu générer de réponse.";
                    }

                } catch (Exception ex) {
                    reply = "L'assistant bancaire est indisponible. "
                            + "Veuillez réessayer plus tard.";
                }

                if (reply.length() > 1900) {
                    reply = reply.substring(0, 1900) + "...";
                }

                hook.editOriginal(reply)
                        .setAllowedMentions(Collections.emptyList())
                        .queue();
            });

        });
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
