package uk.co.angrybee.joe.commands.discord;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import org.apache.commons.lang3.StringUtils;

import uk.co.angrybee.joe.DiscordClient;
import uk.co.angrybee.joe.DiscordResponses;
import uk.co.angrybee.joe.DiscordWhitelister;
import uk.co.angrybee.joe.Utils;
import uk.co.angrybee.joe.sql.MySqlClient;
import uk.co.angrybee.joe.sql.Person;

public class CommandIdentifySelf {
	
    public static void ExecuteCommand(SlashCommandEvent event, String mc_user) {
    	
        User author = event.getUser();
        
        String mcId = Utils.minecraftUsernameToUUID(mc_user);
        
        //if author is already identified, method returns
        Person searched;
        searched = MySqlClient.searchPerson(mcId, "", author.getId(), "");
        if (searched != null) {
            DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeSimpleInfoMessage("you have already identified yourself as '" + searched.getMinecraftName() + "'. If this is an error, contact an administrator"));
            return;
        }
        
        
    	Person mcIdMatch = MySqlClient.searchPerson(mcId, "", "", "");
    	Person discordIdMatch = MySqlClient.searchPerson("", "", author.getId(), "");
    	
    	if (mcIdMatch != null && discordIdMatch != null && !mcIdMatch.equals(discordIdMatch)) {
    		DiscordWhitelister.getPluginLogger().severe("ERROR: two separate db rows have been found for a singular user. this should never happen! FIX ME (CommandIdentifySelf)");
    		DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeErrorMessage());
    		return;
    	}
    	
    	
    	boolean isSuccessful = false;
    	boolean isBanned = false;
    	if (mcIdMatch == null && discordIdMatch == null) {
        	Person p = MySqlClient.insertPerson(mcId, mc_user, author.getId(), author.getName(), false, false);
        	if (p != null) {
        		isSuccessful = true;
        	} else {
        		DiscordWhitelister.getPluginLogger().severe("Failed to add new player into DB during Identify Self (mc_user: " + mc_user + ")");
        		DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeErrorMessage());
        		return;
        	}
        	
    	} else if (mcIdMatch != null) {
    		if (mcIdMatch.getDiscordId() != 0) {
                DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeSimpleInfoMessage("'" + mc_user + "' already belongs to " + mcIdMatch.getDiscordName() + ". If this is an error, contact an administrator"));
                return;
    		}
    		
    		mcIdMatch.setDiscordId(author.getIdLong());
    		mcIdMatch.setDiscordName(author.getName());
    		
    		MySqlClient.updatePerson(mcIdMatch);
    		isSuccessful = true;
    		if (mcIdMatch.isBanned()) isBanned = true;

    	} else if (discordIdMatch != null) {
    		if (!StringUtils.isEmpty(discordIdMatch.getMinecraftName())) {
    			DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeSimpleInfoMessage("you have already identified yourself as '" + discordIdMatch.getMinecraftName() + "'. If this is an error, contact an administrator"));
                return;
    		}
    		
    		discordIdMatch.setMinecraftUUID(mcId);
    		discordIdMatch.setMinecraftName(mc_user);
    		
    		MySqlClient.updatePerson(discordIdMatch);
    		isSuccessful = true;
    		if (discordIdMatch.isBanned()) isBanned = true;
    	}
    	
    	if (isSuccessful) {
    		DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getIdentifySelfSuccess(author, mc_user));
    		if (!isBanned) {
    			DiscordClient.AssignRoleToUser(author.getId(), DiscordWhitelister.mainConfig.getFileConfiguration().getString("member-role"));
    		} else {
    			DiscordClient.AssignRoleToUser(author.getId(), DiscordWhitelister.mainConfig.getFileConfiguration().getString("banned-role"));
    		}
    		DiscordWhitelister.getPluginLogger().info("Discord user " + author.getName() + " (" + author.getId() + ") has identified themselves as '" + mc_user + "'");
    	}

    }
}