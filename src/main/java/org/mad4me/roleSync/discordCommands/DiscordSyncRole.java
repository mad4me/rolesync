package org.mad4me.roleSync.discordCommands;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class DiscordSyncRole extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        if (event.getComponentId().equals("enter-code")) {

            TextInput body = TextInput.create("code", "Код", TextInputStyle.SHORT)
                    .setPlaceholder("Код")
                    .setRequiredRange(4, 4)
                    .build();

            Modal modal = Modal.create("enter-code", "Синхронизация ролей")
                    .addComponents(ActionRow.of(body))
                    .build();

            event.replyModal(modal).queue();
        }
    }
}