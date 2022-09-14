package uk.co.angrybee.joe.commands.discord;

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

public class CommandRemove {

    public static void ExecuteCommand(SlashCommandEvent event, String mc_user) {

        AuthorPermissions authorPermissions = new AuthorPermissions(event);
        User author = event.getUser();
        
        DiscordWhitelister.getPlugin().getLogger().info(author.getName() + "(" + author.getId() + ") attempted to remove " + mc_user + " from the whitelist");
        
        //check author permissions. if insufficient, method returns
        if (!authorPermissions.isUserCanRemove()) {
        	DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getInsufficientPerms(author, "whitelist remove"));
            return;
        }
        
        //user validation. if validation fails, method returns
    	UsernameValidation uvr = Utils.checkMcUsername(mc_user);
    	if (uvr != UsernameValidation.SUCCESS) {
    		DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getInvalidUsername(author, mc_user, uvr));
    		return;
    	}
    	
        //check if user is on the whitelist. if not, method returns
        Person p = MySqlClient.get().searchPerson(mc_user, "", "");
        if (p == null || !p.isWhitelisted()) {
            DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getUserNotOnWhitelist(author, mc_user));
            return;
        }
        
        //execute wl add command
        DiscordWhitelister.ExecuteServerCommand("whitelist remove " + mc_user);
        //save wl event to db
        MySqlClient.get().logWhitelistEvent(author.getId(), WhitelistEventType.REMOVE, mc_user);

        //make and display message
        MessageEmbed whitelistSuccessEmbed = DiscordResponses.getWhitelistRemoveSuccess(author, mc_user);
        DiscordWhitelister.getPlugin().getServer().getScheduler().callSyncMethod(DiscordWhitelister.getPlugin(), () ->
        {
        	event.replyEmbeds(whitelistSuccessEmbed).queue();
            return null;
        });
    }

}