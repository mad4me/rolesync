package org.mad4me.roleSync;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;

import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.configuration.file.FileConfiguration;
import org.mad4me.roleSync.discordCommands.DiscordSyncRole;
import org.mad4me.roleSync.discordCommands.ModalSyncRole;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Discord {

    private final JDA jda;
    private final FileConfiguration config;
    private Guild guild ;

    public Discord(Map<String, Integer> codes, SQLite sql, FileConfiguration config) throws InterruptedException {
        this.config = config;
        this.jda = JDABuilder.createDefault(config.getString("token"))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new DiscordSyncRole()).addEventListeners(new ModalSyncRole(codes, sql, this)).build().awaitReady();
    }

    public Guild getGuild() {
        if (guild == null) {
            guild = jda.getGuildById(config.getLong("server_id"));
        }

        return guild;
    }

    public void start() {

        TextChannel channel = jda.getTextChannelById(config.getLong("channel_id"));
        Stream<Message> messages = channel.getHistory().retrievePast(50).complete().stream()
                .filter(message -> message.getEmbeds().stream()
                        .anyMatch(messageEmbed -> messageEmbed.getTitle().equals("Синхронизировать роли")));

        if (messages.toList().isEmpty()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed
                    .setTitle("Синхронизировать роли")
                    .setDescription("Для того чтобы синхронизировать роли, введите в майнкрафте `/syncorole`, нажмите на кнопку ниже и введите код в открывшемся окне. ")
                    .setColor(0xff0083);

            channel.sendMessage("").setEmbeds(embed.build()).addActionRow(
                    Button.primary("enter-code", "Синхронизовать роли")
            ).queue();
        }
    }

    public Member getMember(long discordId) {
        Guild guild = getGuild();
        guild.retrieveMemberById(discordId).complete();

        return guild.getMemberById(discordId);
    }

    public void giveSub(long discordId) {
        Member member = getMember(discordId);
        guild.addRoleToMember(member, jda.getRoleById(config.getString("premium_role_id"))).queue();
    }

    public void giveUser(long discordId) {
        Member member = getMember(discordId);
        guild.addRoleToMember(member, jda.getRoleById(config.getString("user_role_id"))).queue();
    }

    public void removeSub(long discordId) {
        Member member = getMember(discordId);
        guild.removeRoleFromMember(member, jda.getRoleById(config.getString("premium_role_id"))).queue();
    }

    public boolean hasSubRole(long discordId) {
        Member member = getMember(discordId);

        if (member == null) return false;

        List<Role> memberRoles = member.getRoles();
        for (Role role: memberRoles) {
            if (role == jda.getRoleById(config.getString("premium_role_id"))) return true;
        }

        return false;
    }

    public boolean hasUserRole(long discordId) {
        Member member = getMember(discordId);

        if (member == null) return false;

        List<Role> memberRoles = member.getRoles();
        for (Role role: memberRoles) {
            if (role == jda.getRoleById(config.getString("premium_role_id"))) return true;
        }

        return false;
    }


    public void stop() {
        // TODO fix error when shutting down5
        jda.shutdownNow();
    }
}