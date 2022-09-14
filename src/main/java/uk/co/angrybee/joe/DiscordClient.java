package uk.co.angrybee.joe;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.configuration.file.FileConfiguration;
import uk.co.angrybee.joe.commands.discord.*;
import uk.co.angrybee.joe.events.ShutdownEvents;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

// handles Discord interaction
public class DiscordClient extends ListenerAdapter {
    public static String[] allowedToRemoveRoles;
    public static String[] allowedToAddRoles;

    private static String[] targetTextChannels;

    public static JDA javaDiscordAPI;
    private static Guild guild;

    public static int InitializeClient(String clientToken) {
        AssignVars();

        try {
            javaDiscordAPI = JDABuilder.createDefault(clientToken)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setBulkDeleteSplittingEnabled(false)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOTE)
                    .setContextEnabled(true)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .addEventListeners(new DiscordClient())
                    .addEventListeners(new ShutdownEvents())
                    .build();

            javaDiscordAPI.awaitReady();


            CommandListUpdateAction commands = javaDiscordAPI.updateCommands();

            commands.addCommands(
                    new CommandData("whitelist", "Edit the whitelist.")
                            .addSubcommands(
                                    new SubcommandData("add", "Add a user to the whitelist")
                                            .addOption(STRING, "minecraft_username", "Minecraft username to add", true),
                                    new SubcommandData("remove", "Remove user from the whitelist")
                                            .addOption(STRING, "minecraft_username", "Minecraft username to remove", true)),
                            
                    new CommandData("identify", "link minecraft names to discord names")
                    		.addSubcommands(
                    				new SubcommandData("self", "Set your in-game Minecraft username (required)")
                    						.addOption(STRING, "minecraft_username", "Your in-game Minecraft username", true),
                    				new SubcommandData("user", "Find the in-game Minecraft username for a given Discord user")
                    						.addOption(USER, "discord_user", "Discord user to look for", true)))
                    .queue();
            
            guild = javaDiscordAPI.getGuildById(DiscordWhitelister.mainConfig.getFileConfiguration().getString("guild-id"));
            
            return 0;
            
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        } catch (IllegalStateException e) {
            // Don't print exception related to disallowed intents, already handled
            if (!e.getMessage().startsWith("Was shutdown trying to await status"))
                e.printStackTrace();

            return 1;
        }
    }

    public static boolean ShutdownClient() {
        javaDiscordAPI.shutdownNow();

        return javaDiscordAPI.getStatus() == JDA.Status.SHUTTING_DOWN || javaDiscordAPI.getStatus() == JDA.Status.SHUTDOWN;
    }

    private static void AssignVars() {
        FileConfiguration mainConfig = DiscordWhitelister.mainConfig.getFileConfiguration();

        targetTextChannels = new String[mainConfig.getList("target-text-channels").size()];
        for (int i = 0; i < targetTextChannels.length; ++i) {
            targetTextChannels[i] = mainConfig.getList("target-text-channels").get(i).toString();
        }
    }

    public static String getOnlineStatus() {
        try {
            return javaDiscordAPI.getStatus().name();
        } catch (NullPointerException ex) {
            return "OFFLINE";
        }
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {


        if (!event.getGuild().getId().equals(DiscordWhitelister.mainConfig.getFileConfiguration().getString("guild-id"))) {
            MessageEmbed messageEmbed = DiscordResponses.makeSimpleInfoMessage("This bot can only used in AMFPD");
            ReplyAndRemoveAfterSeconds(event, messageEmbed);
            return;
        }

        if (!Arrays.asList(targetTextChannels).contains(event.getTextChannel().getId())) {
            MessageEmbed messageEmbed = DiscordResponses.makeSimpleInfoMessage("This command can only used in the #whitelist channel.");
            ReplyAndRemoveAfterSeconds(event, messageEmbed);
            return;
        }

        String subcommand = event.getSubcommandName();
        OptionMapping mc_name_op = event.getOption("minecraft_username");
        String mc_name = null;
        if (mc_name_op != null) {
            mc_name = mc_name_op.getAsString();
        }
        OptionMapping dc_name_op = event.getOption("discord_user");
        User dc_user = null;
        if (dc_name_op != null) {
            dc_user = dc_name_op.getAsUser();
        }

        switch (event.getName()) {
            case "whitelist": {
                if (subcommand != null) {
                    switch (subcommand) {
                        case "add": {
                            //!whitelist add command:
                            CommandAdd.ExecuteCommand(event, mc_name);
                        }
                        break;
                        case "remove": {
                            // Remove Command
                            CommandRemove.ExecuteCommand(event, mc_name);
                        }
                        break;
                    }
                }
            }
            break;
            case "identify": {
            	if (subcommand != null) {
            		switch (subcommand) {
	            		case "self": {
	            			CommandIdentifySelf.ExecuteCommand(event, mc_name);
	            		}
	            		break;
	            		case "user": {
	            			CommandIdentifyUser.ExecuteCommand(event, dc_user);
	            		}
	            		break;
            		}
            	}
            }
            break;

            default:
                event.reply("unrecognized command.").setEphemeral(true).queue();
        }


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent messageReceivedEvent) {
        if (!messageReceivedEvent.isFromType(ChannelType.TEXT)) {
            return;
        }
        // Check if message should be handled
        if (!Arrays.asList(targetTextChannels).contains(messageReceivedEvent.getTextChannel().getId()))
            return;

        if (messageReceivedEvent.getAuthor().getIdLong() == javaDiscordAPI.getSelfUser().getIdLong())
            return;

        TextChannel channel = messageReceivedEvent.getTextChannel();

        // if no commands are executed, delete the message, if enabled
        if (DiscordWhitelister.removeUnnecessaryMessages) {
            RemoveMessageAfterSeconds(messageReceivedEvent, DiscordWhitelister.removeMessageWaitTime);
        }
        
        //Warn user that this is for commands only
        QueueAndRemoveAfterSeconds(channel, DiscordResponses.getCommandOnly());
    }








    public static void AssignRoleToUser(String targetUserId, String rollId) { 
        Role role = guild.getRoleById(rollId);
        if (role == null) {
            DiscordWhitelister.getPluginLogger().warning("Failed to assign role " + rollId
                    + " to user " + targetUserId + " as it could not be found in "
                    + guild.getName());
        } else {
        	guild.addRoleToMember(guild.getMemberById(targetUserId), role);
        }
    }

    public static void RemoveRoleFromUser(String targetUserId, String rollId) {
        Role role = guild.getRoleById(rollId);
        if (role == null) {
            DiscordWhitelister.getPluginLogger().warning("Failed to remove role " + rollId
                    + " to user " + targetUserId + " as it could not be found in "
                    + guild.getName());
        } else {
        	guild.removeRoleFromMember(guild.getMemberById(targetUserId), role);
        }

    }

    public static void RemoveMessageAfterSeconds(MessageReceivedEvent messageReceivedEvent, Integer timeToWait) {
        Thread removeTimerThread = new Thread(() ->
        {
            try {
                TimeUnit.SECONDS.sleep(timeToWait);
                messageReceivedEvent.getMessage().delete().queue();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        removeTimerThread.start();
    }


    public static void ReplyAndRemoveAfterSeconds(SlashCommandEvent event, MessageEmbed messageEmbed) {
        if (DiscordWhitelister.removeUnnecessaryMessages)
            event.replyEmbeds(messageEmbed).queue(message -> message.deleteOriginal().queueAfter(DiscordWhitelister.removeMessageWaitTime, TimeUnit.SECONDS));
        else
            event.replyEmbeds(messageEmbed).queue();
    }

    public static void QueueAndRemoveAfterSeconds(TextChannel textChannel, MessageEmbed messageEmbed) {
        if (DiscordWhitelister.removeUnnecessaryMessages)
            textChannel.sendMessageEmbeds(messageEmbed).queue(message -> message.delete().queueAfter(DiscordWhitelister.removeMessageWaitTime, TimeUnit.SECONDS));
        else
            textChannel.sendMessageEmbeds(messageEmbed).queue();
    }
}
