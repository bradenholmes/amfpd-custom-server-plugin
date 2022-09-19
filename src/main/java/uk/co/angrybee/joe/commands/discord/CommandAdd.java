package uk.co.angrybee.joe.commands.discord;

import java.util.logging.Level;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import uk.co.angrybee.joe.AuthorPermissions;
import uk.co.angrybee.joe.DiscordClient;
import uk.co.angrybee.joe.DiscordResponses;
import uk.co.angrybee.joe.DiscordWhitelister;
import uk.co.angrybee.joe.Utils;
import uk.co.angrybee.joe.Utils.UsernameValidation;
import uk.co.angrybee.joe.Utils.WhitelistEventType;
import uk.co.angrybee.joe.sql.MySqlClient;
import uk.co.angrybee.joe.sql.Person;

public class CommandAdd {

    public static void ExecuteCommand(SlashCommandEvent event, String mc_user) {
        AuthorPermissions authorPermissions = new AuthorPermissions(event);
        User author = event.getUser();
        
        
        
		Person caller = MySqlClient.get().searchPerson("", author.getId(), "");
		if (caller == null) {
			DiscordWhitelister.getPluginLogger().log(Level.SEVERE, "Unidentified user attempted to add to the whitelist");
			return;
		}
		
		DiscordWhitelister.getPlugin().getLogger().info(author.getName() + "(" + author.getId() + ") attempted to whitelist: " + mc_user);
        
        //check author permissions. if insufficient, method returns
        if (!authorPermissions.isUserCanAdd()) {
        	DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getInsufficientPerms(author, "whitelist add"));
            return;
        }
        
        //user validation. if validation fails, method returns
    	UsernameValidation uvr = Utils.checkMcUsername(mc_user);
    	if (uvr != UsernameValidation.SUCCESS) {
    		DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getInvalidUsername(author, mc_user, uvr));
    		return;
    	}



        //check if user is already on the whitelist. if so, method returns
        Person subject = MySqlClient.get().searchPerson(mc_user, "", "");
        if (subject != null) {
        	if (subject.isWhitelisted()) {
                DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getUserAlreadyOnWhitelist(author, mc_user));
                return;
        	}
        	if (subject.isBanned()) {
                DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getUserBanned(author, mc_user));
                return;
        	}

        } else {
        	MySqlClient.get().insertPerson(mc_user, "", "", true, false);
        }
        
        subject = MySqlClient.get().searchPerson(mc_user, "", "");
		if (subject == null) {
			DiscordWhitelister.getPluginLogger().log(Level.SEVERE, "Failed to make a new Person object out of mc_user '" + mc_user + "'");
			DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.makeErrorMessage());
			return;
		} else {
			subject.setWhitelisted(true);
			MySqlClient.get().updatePerson(subject);
		}
		
		
        
        
        //execute wl add command
        DiscordWhitelister.ExecuteServerCommand("whitelist add " + mc_user);
        //save wl event to db
        MySqlClient.get().logWhitelistEvent(caller.getPrimaryId(), WhitelistEventType.ADD, subject.getPrimaryId());
        
        DiscordWhitelister.getPlugin().getLogger().info(author.getName() + "(" + author.getId() + ") successfully whitelisted: " + mc_user);

        //make and display message
        MessageEmbed whitelistSuccessEmbed = DiscordResponses.getWhitelistAddSuccess(author, mc_user);
        DiscordWhitelister.getPlugin().getServer().getScheduler().callSyncMethod(DiscordWhitelister.getPlugin(), () ->
        {
        	event.replyEmbeds(whitelistSuccessEmbed).queue();
            return null;
        });
    }
}