package org.mad4me.roleSync.commands;

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

        if (codes.containsKey(uuid)) {
            commandSender.sendMessage("Вы уже отправляли код: " + codes.get(uuid));
            return false;
        }

        if (sql.isPlayerLinked(uuid)) {
            commandSender.sendMessage("Ваш акаунт уже привязан.");
            return false;
        }

        Integer code = generateCode();
        codes.put(uuid, code);

        commandSender.sendMessage("Код для верификаций: " + codes.get(uuid));
        return true;
    }

    public static Integer generateCode() {
        Random random = new Random();
        return random.nextInt(9000) + 1000;
    }

}

