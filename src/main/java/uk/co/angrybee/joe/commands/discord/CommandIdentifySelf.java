package uk.co.angrybee.joe.commands.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import uk.co.angrybee.joe.AuthorPermissions;
import uk.co.angrybee.joe.DiscordClient;
import uk.co.angrybee.joe.sql.MySqlClient;
import uk.co.angrybee.joe.sql.Person;

public class CommandIdentifySelf {
	
    public static void ExecuteCommand(SlashCommandEvent event, String mc_name) {
    	
        AuthorPermissions authorPermissions = new AuthorPermissions(event);
        User author = event.getUser();
        TextChannel channel = event.getTextChannel();
        
        Person searched;
        searched = MySqlClient.get().searchPerson(mc_name, author.getId(), "");
        if (searched == null) {
        	Person mcNameMatch = MySqlClient.get().searchPerson(mc_name, "", "");
        	Person discordIdMatch = MySqlClient.get().searchPerson("", author.getId(), "");
        	
        	if (mcNameMatch == null && discordIdMatch == null) {
            	MySqlClient.get().insertPerson(mc_name, author.getId(), author.getName());
                EmbedBuilder insertedMessage = DiscordClient.CreateEmbeddedMessage("Success!", ("Thank you for identifying yourself as '" + mc_name + "'"),
                        DiscordClient.EmbedMessageType.SUCCESS);
                DiscordClient.ReplyAndRemoveAfterSeconds(event, insertedMessage.build());
                List<String> targetRoll = new ArrayList<>();
                targetRoll.add("Member");
                DiscordClient.AssignRolesToUser(event.getGuild(), author.getId(), targetRoll);
                return;
        	} else if (mcNameMatch != null) {
        		if (mcNameMatch.getDiscordId() == 0) {
        			MySqlClient.get().updatePerson(mcNameMatch.getPrimaryId(), mcNameMatch.getMinecraftName(), author.getId(), author.getName());
                    EmbedBuilder insertedMessage = DiscordClient.CreateEmbeddedMessage("Success!", ("Thank you for identifying yourself as '" + mc_name + "'"),
                            DiscordClient.EmbedMessageType.SUCCESS);
                    DiscordClient.ReplyAndRemoveAfterSeconds(event, insertedMessage.build());
                    List<String> targetRoll = new ArrayList<>();
                    targetRoll.add("Member");
                    DiscordClient.AssignRolesToUser(event.getGuild(), author.getId(), targetRoll);
                    return;
        		} else {
                    EmbedBuilder insertedMessage = DiscordClient.CreateEmbeddedMessage("Failed!", ("'" + mc_name + "' already belongs to " + mcNameMatch.getDiscordName() + ". If this is an error, contact an administrator"),
                            DiscordClient.EmbedMessageType.FAILURE);
                    DiscordClient.ReplyAndRemoveAfterSeconds(event, insertedMessage.build());
                    return;
        		}
        	} else if (discordIdMatch != null) {
        		if (StringUtils.isEmpty(discordIdMatch.getMinecraftName())) {
        			MySqlClient.get().updatePerson(discordIdMatch.getPrimaryId(), discordIdMatch.getMinecraftName(), author.getId(), author.getName());
                    EmbedBuilder insertedMessage = DiscordClient.CreateEmbeddedMessage("Success!", ("Thank you for identifying yourself as '" + mc_name + "'"),
                            DiscordClient.EmbedMessageType.SUCCESS);
                    DiscordClient.ReplyAndRemoveAfterSeconds(event, insertedMessage.build());
                    List<String> targetRoll = new ArrayList<>();
                    targetRoll.add("Member");
                    DiscordClient.AssignRolesToUser(event.getGuild(), author.getId(), targetRoll);
                    return;
        		} else {
                    EmbedBuilder insertedMessage = DiscordClient.CreateEmbeddedMessage("Failed!", ("you have already identified yourself as '" + discordIdMatch.getMinecraftName() + "'. If this is an error, contact an administrator"),
                            DiscordClient.EmbedMessageType.FAILURE);
                    DiscordClient.ReplyAndRemoveAfterSeconds(event, insertedMessage.build());
                    return;
        		}
        	}
        } else {
            EmbedBuilder insertedMessage = DiscordClient.CreateEmbeddedMessage("", ("you have already identified yourself as '" + mc_name + "'. If this is an error, contact an administrator"),
                    DiscordClient.EmbedMessageType.INFO);
            DiscordClient.ReplyAndRemoveAfterSeconds(event, insertedMessage.build());
            return;
        }


/*

		        if (!authorPermissions.isUserCanAddRemove() && !authorPermissions.isUserCanAdd()) {
            DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordClient.CreateInsufficientPermsMessage(author));
            return;
        }


        boolean idFound = false;
        // Find the Discord Id linked to the whitelisted player
        Set<String> keys = UserList.getUserList().getKeys(false);
        for (
                String discordId : keys) {
            List<?> registeredUsers = UserList.getRegisteredUsers(discordId);
            for (Object name : registeredUsers) {
                if (name.equals(mc_name)) {
                    String userAsMention = "<@!" + discordId + ">"; // use this in-case the user has left the discord ? over using fetched member
                    StringBuilder usersWhitelistedPlayers = new StringBuilder();
                    for (Object targetWhitelistedPlayer : registeredUsers) {
                        if (targetWhitelistedPlayer instanceof String)
                            usersWhitelistedPlayers.append("- ").append((String) targetWhitelistedPlayer).append("\n");
                    }

                    EmbedBuilder idFoundMessage = DiscordClient.CreateEmbeddedMessage(("Found account linked to `" + mc_name + "`"),
                            (author.getAsMention() + ", the Minecraft username: `" + mc_name + "` is linked to " + userAsMention +
                                    ".\n\n Here is a list of their whitelisted players:\n" + usersWhitelistedPlayers),
                            DiscordClient.EmbedMessageType.SUCCESS);

                    User fetchedUser = DiscordClient.javaDiscordAPI.getUserById(discordId);

                    if (fetchedUser != null)
                        idFoundMessage.setThumbnail(fetchedUser.getAvatarUrl());
                    else
                        DiscordWhitelister.getPluginLogger().warning("Failed to fetch avatar linked to Discord ID: " + discordId);

                    DiscordClient.ReplyAndRemoveAfterSeconds(event, idFoundMessage.build());
                    idFound = true;
                    break;
                }
            }
        }
        if (!idFound) {
            MessageEmbed messageEmbed = DiscordClient.CreateEmbeddedMessage(("Could not find an account linked to `" + mc_name + "`"),
                    (author.getAsMention() + ", the name: `" + mc_name +
                            "` could not be found in the users list. Please make sure that the Minecraft name is valid and whitelisted + linked to an ID before."),
                    DiscordClient.EmbedMessageType.FAILURE).build();

            DiscordClient.ReplyAndRemoveAfterSeconds(event, messageEmbed);
        }
        */
    }
}