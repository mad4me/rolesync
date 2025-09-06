package org.mad4me.roleSync.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.mad4me.roleSync.SQLite;

import java.util.Map;
import java.util.Random;

public class SyncRole implements CommandExecutor {

    private final Map<String, Integer> codes;
    private final SQLite sql;

    public SyncRole(Map<String, Integer> codes, SQLite sql) {
        this.codes = codes;
        this.sql = sql;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Это консоль брат ._.");
            return false;
        }

        String uuid = ((Player) commandSender).getUniqueId().toString();

        var mm = MiniMessage.miniMessage();

        if (codes.containsKey(uuid)) {
            Component parsed = mm.deserialize("<gradient:#FF0484:#ffffff>Zeffyr</gradient> <#7c7c7c>> <gradient:#EDEDED:#bebebe>Вы уже вводили код - " + codes.get(uuid) + "</gradient>" );
            commandSender.sendMessage(parsed);
            return false;
        }

        if (sql.isPlayerLinked(uuid)) {
            Component parsed =  mm.deserialize("<gradient:#FF0484:#ffffff>Zeffyr</gradient> <#7c7c7c>> <gradient:#EDEDED:#bebebe>Ваш акаунт уже привязан.</gradient>");
            commandSender.sendMessage(parsed);
            return false;
        }

        Integer code = generateCode();
        codes.put(uuid, code);

        Component parsed =  mm.deserialize("<gradient:#FF0484:#ffffff>Zeffyr</gradient> <#7c7c7c>> <gradient:#EDEDED:#bebebe>Код верификации - " + codes.get(uuid) +"</gradient>");
        commandSender.sendMessage(parsed);
        return true;
    }

    public static Integer generateCode() {
        Random random = new Random();
        return random.nextInt(9000) + 1000;
    }

}

