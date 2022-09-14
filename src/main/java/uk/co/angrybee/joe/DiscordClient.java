package uk.co.angrybee.joe;

import net.dv8tion.jda.api.EmbedBuilder;
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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import uk.co.angrybee.joe.commands.discord.*;
import uk.co.angrybee.joe.events.ShutdownEvents;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

// handles Discord interaction
public class DiscordClient extends ListenerAdapter {
    public static String[] allowedToRemoveRoles;
    public static String[] allowedToAddRoles;

    private static String[] targetTextChannels;
    
    public static MessageEmbed botInfo;
    public static MessageEmbed addCommandInfo;
    public static MessageEmbed removeCommandInfo;
    public static MessageEmbed whoIsInfo;


    public static boolean usernameValidation;

    public static final char[] validCharacters = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h',
            'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '_'};

    public static JDA javaDiscordAPI;

    public static int InitializeClient(String clientToken) {
        AssignVars();
        BuildStrings();

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
                    						.addOption(USER, "discord_user", "Discord user to look for", true)),
                    		
                    new CommandData("clearname", "Clear name from all lists")
                            .addOption(STRING, "minecraft_username", "Minecraft username to clear", true),
                    new CommandData("clearban", "Clear ban from user")
                            .addOption(STRING, "minecraft_username", "Minecraft username to unban", true))
                    .queue();

            // Send the new set of commands to discord, this will override any existing global commands with the new set provided here


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

        // assign vars here instead of every time a message is received, as they do not change
        targetTextChannels = new String[mainConfig.getList("target-text-channels").size()];
        for (int i = 0; i < targetTextChannels.length; ++i) {
            targetTextChannels[i] = mainConfig.getList("target-text-channels").get(i).toString();
        }

        usernameValidation = mainConfig.getBoolean("username-validation");

    }

    private static void BuildStrings() {
        // build here instead of every time a message is received, as they do not change
        EmbedBuilder embedBuilderBotInfo = new EmbedBuilder();
        embedBuilderBotInfo.setTitle("Discord Whitelister for Spigot");
        embedBuilderBotInfo.addField("Version", VersionInfo.getVersion(), false);
        embedBuilderBotInfo.addField("Links", ("https://www.spigotmc.org/resources/discord-whitelister.69929/\nhttps://github.com/JoeShimell/DiscordWhitelisterSpigot"), false);
        embedBuilderBotInfo.addField("Commands", ("**Add:** /whitelist add minecraftUsername\n**Remove:** /whitelist remove minecraftUsername"), false);
        embedBuilderBotInfo.addField("Experiencing issues?", "If you encounter an issue, please report it here: https://github.com/JoeShimell/DiscordWhitelisterSpigot/issues", false);
        embedBuilderBotInfo.setColor(infoColour);
        botInfo = embedBuilderBotInfo.build();

        addCommandInfo = CreateEmbeddedMessage("Whitelist Add Command",
                "/whitelist add minecraftUsername\n\nIf you encounter any issues, please report them here: https://github.com/JoeShimell/DiscordWhitelisterSpigot/issues",
                EmbedMessageType.INFO).build();

        removeCommandInfo = CreateEmbeddedMessage("Whitelist Remove Command",
                "/whitelist remove minecraftUsername\n\nIf you encounter any issues, please report them here: https://github.com/JoeShimell/DiscordWhitelisterSpigot/issues",
                EmbedMessageType.INFO).build();
    }

    public static String getOnlineStatus() {
        try {
            return javaDiscordAPI.getStatus().name();
        } catch (NullPointerException ex) {
            return "OFFLINE";
        }
    }

    public enum EmbedMessageType {INFO, SUCCESS, FAILURE}

    private static final Color infoColour = new Color(104, 109, 224);
    private static final Color successColour = new Color(46, 204, 113);
    private static final Color failureColour = new Color(231, 76, 60);

    public static EmbedBuilder CreateEmbeddedMessage(String title, String message, EmbedMessageType messageType) {
        EmbedBuilder newMessageEmbed = new EmbedBuilder();
        newMessageEmbed.addField(title, message, false);

        if (messageType == EmbedMessageType.INFO)
            newMessageEmbed.setColor(infoColour);
        else if (messageType == EmbedMessageType.SUCCESS)
            newMessageEmbed.setColor(successColour);
        else if (messageType == EmbedMessageType.FAILURE)
            newMessageEmbed.setColor(failureColour);
        else
            newMessageEmbed.setColor(new Color(255, 255, 255));

        return newMessageEmbed;
    }


    public static MessageEmbed CreateInsufficientPermsMessage(User messageAuthor) {
        MessageEmbed insufficientMessageEmbed;

        String customTitle = "TITLE";
        String customMessage = "{Sender}, you've got some 'splainin to do";
        customMessage = customMessage.replaceAll("\\{Sender}", messageAuthor.getAsMention()); // Only checking for {Sender}

        insufficientMessageEmbed = CreateEmbeddedMessage(customTitle, customMessage, EmbedMessageType.FAILURE).build();

        return insufficientMessageEmbed;
    }

    public static MessageEmbed CreateInstructionalMessage() {
        MessageEmbed instructionalMessageEmbed;


        String addCommandExample = "/whitelist add";

        instructionalMessageEmbed = CreateEmbeddedMessage("How to Whitelist", ("Use `" + addCommandExample + " <minecraftUsername>` to whitelist yourself.\n" +
                "In the case of whitelisting an incorrect name, please contact a staff member to clear it from the whitelist."), EmbedMessageType.INFO).build();


        return instructionalMessageEmbed;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {


        if (event.getGuild() == null) {
            MessageEmbed messageEmbed = CreateEmbeddedMessage("Sorry!",
                    ("This bot can only used in AMFPD for now... contact @oddlyMetered"), EmbedMessageType.FAILURE).build();
            ReplyAndRemoveAfterSeconds(event, messageEmbed);
            return;
        }

        if (!Arrays.asList(targetTextChannels).contains(event.getTextChannel().getId())) {
            MessageEmbed messageEmbed = CreateEmbeddedMessage("Sorry!",
                    ("This bot can only used in #whitelist channel."), EmbedMessageType.FAILURE).build();
            ReplyAndRemoveAfterSeconds(event, messageEmbed);
            return;
        }

        String subcommand = event.getSubcommandName();
        OptionMapping mc_name_op = event.getOption("minecraft_username");
        String mc_name = null;
        if (mc_name_op != null) {
            mc_name = mc_name_op.getAsString();
        }
        OptionMapping dc_name_op = event.getOption("discord_user"); // the "user" option is required so it doesn't need a null-check here
        Member dc_name = null;
        long dc_id = 0;
        if (dc_name_op != null) {
            dc_name = dc_name_op.getAsMember();
            dc_id = dc_name_op.getAsUser().getIdLong();
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
	            			CommandIdentifyUser.ExecuteCommand(event, dc_id);
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


        // TODO remove, use in command classes when complete
        User author = messageReceivedEvent.getAuthor();
        TextChannel channel = messageReceivedEvent.getTextChannel();

        // if no commands are executed, delete the message, if enabled
        if (DiscordWhitelister.removeUnnecessaryMessages) {
            RemoveMessageAfterSeconds(messageReceivedEvent, DiscordWhitelister.removeMessageWaitTime);
        }
        
        //Warn user that this is for commands only
        MessageEmbed messageEmbed = CreateEmbeddedMessage("Commands Only Channel", (author.getAsMention() + ", this channel is for commands only, please use #general to chat or #issues to report a problem"),
                EmbedMessageType.INFO).build();
        QueueAndRemoveAfterSeconds(channel, messageEmbed);


    }


    public static String minecraftUsernameToUUID(String minecraftUsername) {
        URL playerURL;
        String inputStream;
        BufferedReader bufferedReader;

        String playerUUID = null;

        try {
            playerURL = new URL("https://api.mojang.com/users/profiles/minecraft/" + minecraftUsername);
            bufferedReader = new BufferedReader(new InputStreamReader(playerURL.openStream()));
            inputStream = bufferedReader.readLine();

            if (inputStream != null) {
                JSONObject inputStreamObject = (JSONObject) JSONValue.parseWithException(inputStream);
                playerUUID = inputStreamObject.get("id").toString();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return playerUUID;
    }

    public static void ExecuteServerCommand(String command) {
        DiscordWhitelister.getPlugin().getServer().getScheduler().callSyncMethod(DiscordWhitelister.getPlugin(), ()
                -> DiscordWhitelister.getPlugin().getServer().dispatchCommand(
                DiscordWhitelister.getPlugin().getServer().getConsoleSender(), command));
    }

    private enum SenderType {CONSOLE, PLAYER, UNKNOWN}

    public static void CheckAndExecuteCommand(String configInput, String playerTargetName) {
        SenderType senderType;

        // Check command sender type
        if (configInput.startsWith("CONSOLE"))
            senderType = SenderType.CONSOLE;
        else if (configInput.startsWith("PLAYER"))
            senderType = SenderType.PLAYER;
        else
            senderType = SenderType.UNKNOWN;

        if (senderType.equals(SenderType.UNKNOWN)) {
            DiscordWhitelister.getPluginLogger().warning("Unknown command sender type (should be one of the following: CONSOLE, PLAYER), offending line: " + configInput);
            return;
        }

        // Get command which is after the first :
        String commandToSend = configInput.substring(configInput.indexOf(":") + 1);
        // Set player name if %PLAYER% is used
        final String commandToSendFinal = commandToSend.replaceAll("%PLAYER%", playerTargetName);

        if (senderType.equals(SenderType.CONSOLE)) {
            DiscordWhitelister.getPlugin().getServer().getScheduler().callSyncMethod(DiscordWhitelister.getPlugin(),
                    () -> DiscordWhitelister.getPlugin().getServer().dispatchCommand(DiscordWhitelister.getPlugin().getServer().getConsoleSender(), commandToSendFinal));
        } else {
            DiscordWhitelister.getPlugin().getServer().getPlayer(playerTargetName).performCommand(commandToSendFinal);
        }
    }


    public static void AssignRoleToUser(Guild guild, String targetUserId, String rollId) { 
        Role role = guild.getRoleById(rollId);
        if (role == null) {
            DiscordWhitelister.getPluginLogger().warning("Failed to assign role " + rollId
                    + " to user " + targetUserId + " as it could not be found in "
                    + guild.getName());
        } else {
        	guild.addRoleToMember(guild.getMemberById(targetUserId), role);
        }
    }

    public static void RemoveRoleFromUser(Guild guild, String targetUserId, String rollId) {
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
