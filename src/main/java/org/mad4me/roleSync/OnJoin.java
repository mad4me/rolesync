package org.mad4me.roleSync;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnJoin implements Listener {

    private final SQLite sql;
    private final Discord bot;

    public OnJoin(SQLite sql, Discord bot) {
        this.sql = sql; this.bot = bot;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        boolean hasSubscribePermission = event.getPlayer().hasPermission("syncroles.sub");
        long discordId = sql.getDiscordId(uuid);

        if (sql.isPlayerLinked(uuid)) {
            if (hasSubscribePermission) {
                // If the user has perms but doesn't have the role, we add it
                if (!bot.hasSubRole(discordId)) bot.giveSub(discordId);
            } else {
                // If the user doesn't have perms but has the role, we remove it it
                if (bot.hasSubRole(discordId)) bot.removeSub(discordId);
            }
        }
    }
}
