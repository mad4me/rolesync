package org.mad4me.roleSync;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mad4me.roleSync.commands.SyncRole;
import org.mad4me.roleSync.commands.SyncRoleReload;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class RoleSync extends JavaPlugin {

    private Discord discordBot;
    private final Map<String, Integer> codes = new ConcurrentHashMap<>();
    private final SQLite sql = new SQLite(this);

    public boolean hasPermission(User user, String permission) {
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    @Override
    public void onEnable() {

        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        // runs the discordBot
        try {
            discordBot = new Discord(codes, sql, config);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        discordBot.start();

        sql.createDatabase();
        sql.createTable();

        // LP Objects
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        UserManager userManager = provider.getProvider().getUserManager();

        getServer().getPluginManager().registerEvents(new OnJoin(sql, discordBot), this);
        this.getCommand("syncrole").setExecutor(new SyncRole(codes, sql));
        this.getCommand("syncrole-reload").setExecutor(new SyncRoleReload(this));

        // player check
        List<String> linkedPlayers = sql.getLinkedPlayers();

        for(String playerUuid: linkedPlayers) {
            UUID uuid = UUID.fromString(playerUuid);
            CompletableFuture<User> userFuture = userManager.loadUser(uuid);

            userFuture.thenAcceptAsync(user -> {
                long discordId = sql.getDiscordId(playerUuid);

                if (discordBot.hasPremiumRole(discordId)) {
                    if (!hasPermission(user, "syncroles.sub")) {
                        // If the user has the premium role, but doesn't have the permission, we remove the role
                        this.getLogger().log(Level.INFO, user.getUsername() + " isn't a sub, removing role.");
                        discordBot.removeRole(discordId);
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {
        discordBot.stop();
        sql.closeConnection();
    }
}
