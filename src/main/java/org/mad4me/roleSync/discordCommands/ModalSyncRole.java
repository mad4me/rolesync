package org.mad4me.roleSync.discordCommands;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.mad4me.roleSync.Discord;
import org.mad4me.roleSync.SQLite;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ModalSyncRole extends ListenerAdapter {

    private final Map<String, Integer> codes;
    private final SQLite sql;
    private final Discord bot;

    public ModalSyncRole(Map<String, Integer> codes, SQLite sql, Discord bot) {
        this.codes = codes;
        this.sql = sql;
        this.bot = bot;
    }

    public boolean hasPermission(User user, String permission) {
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static String findKeyByValue(Map<String, Integer> map, Integer value) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {

        if (event.getModalId().equals("enter-code")) {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

            assert provider != null;
            UserManager userManager = provider.getProvider().getUserManager();

            String stringCode = event.getValue("code").getAsString();

            try {
                int code = Integer.parseInt(stringCode);

                String userId =  event.getUser().getId();

                if (codes.containsValue(code)) {
                    String playerUuid = findKeyByValue(codes, code);

                    sql.insertPlayer(userId, playerUuid);

                    event.reply("Ваш аккаунт успешно залинкован к UUID " + playerUuid ).setEphemeral(true).queue();

                    assert playerUuid != null;
                    UUID uuid = UUID.fromString(playerUuid);

                    CompletableFuture<User> userFuture = userManager.loadUser(uuid);

                    userFuture.thenAcceptAsync(user -> {
                        if (hasPermission(user, "syncroles.sub")) {
                            bot.giveRole(sql.getDiscordId(playerUuid));
                        }
                    });

                    codes.remove(code);

                } else {
                    event.reply("Код не действителен").setEphemeral(true).queue();
                }
            } catch (NumberFormatException e) {
                event.reply("Код не действителен").setEphemeral(true).queue();
            }
        }
    }
}
