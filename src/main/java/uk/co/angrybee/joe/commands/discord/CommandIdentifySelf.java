package uk.co.angrybee.joe.commands.discord;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import org.apache.commons.lang3.StringUtils;
import uk.co.angrybee.joe.DiscordClient;
import uk.co.angrybee.joe.DiscordResponses;
import uk.co.angrybee.joe.DiscordWhitelister;
import uk.co.angrybee.joe.sql.MySqlClient;
import uk.co.angrybee.joe.sql.Person;

public class CommandIdentifySelf {
	
    public static void ExecuteCommand(SlashCommandEvent event, String mc_user) {
    	
        User author = event.getUser();
        
        //if author is already identified, method returns
        Person searched;
        searched = MySqlClient.get().searchPerson(mc_user, author.getId(), "");
        if (searched != null) {
            DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeSimpleInfoMessage("you have already identified yourself as '" + mc_user + "'. If this is an error, contact an administrator"));
            return;
        }
        
        
    	Person mcUserMatch = MySqlClient.get().searchPerson(mc_user, "", "");
    	Person discordIdMatch = MySqlClient.get().searchPerson("", author.getId(), "");
    	
    	if (mcUserMatch != null && discordIdMatch != null && !mcUserMatch.equals(discordIdMatch)) {
    		DiscordWhitelister.getPluginLogger().severe("ERROR: two separate db rows have been found for a singular user. this should never happen! FIX ME (CommandIdentifySelf)");
    		DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeSimpleInfoMessage("a critical error occured, contact an administrator"));
    		return;
    	}
    	
    	
    	boolean isSuccessful = false;
    	if (mcUserMatch == null && discordIdMatch == null) {
        	MySqlClient.get().insertPerson(mc_user, author.getId(), author.getName(), false);
        	isSuccessful = true;
    	} else if (mcUserMatch != null) {
    		if (mcUserMatch.getDiscordId() != 0) {
                DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeSimpleInfoMessage("'" + mc_user + "' already belongs to " + mcUserMatch.getDiscordName() + ". If this is an error, contact an administrator"));
                return;
    		}
    		
    		MySqlClient.get().updatePerson(mcUserMatch.getPrimaryId(), mcUserMatch.getMinecraftName(), author.getId(), author.getName(), mcUserMatch.isWhitelisted());
    		isSuccessful = true;

    	} else if (discordIdMatch != null) {
    		if (!StringUtils.isEmpty(discordIdMatch.getMinecraftName())) {
    			DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeSimpleInfoMessage("you have already identified yourself as '" + mc_user + "'. If this is an error, contact an administrator"));
                return;
    		}
    		
    		MySqlClient.get().updatePerson(discordIdMatch.getPrimaryId(), discordIdMatch.getMinecraftName(), author.getId(), author.getName(), discordIdMatch.isWhitelisted());
    		isSuccessful = true;
    	}
    	
    	if (isSuccessful) {
    		DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getIdentifySelfSuccess(author, mc_user));
    		DiscordClient.AssignRoleToUser(author.getId(), DiscordWhitelister.mainConfig.getFileConfiguration().getString("member-role"));
    		DiscordWhitelister.getPluginLogger().info("Discord user " + author.getName() + " (" + author.getId() + ") has identified themselves as '" + mc_user + "'");
    	}

    }
}