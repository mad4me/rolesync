package org.mad4me.roleSync.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SyncRoleReload implements CommandExecutor {

    private final JavaPlugin plugin;

    public SyncRoleReload(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

        if (!commandSender.hasPermission("syncroles.reload")) {
            commandSender.sendMessage("Нету прав");
            return false;
        }

        plugin.reloadConfig();
        commandSender.sendMessage("Config reloaded");

        return true;
    }

}
