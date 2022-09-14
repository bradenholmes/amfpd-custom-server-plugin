package uk.co.angrybee.joe.commands.discord;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import uk.co.angrybee.joe.DiscordClient;
import uk.co.angrybee.joe.DiscordResponses;
import uk.co.angrybee.joe.sql.MySqlClient;
import uk.co.angrybee.joe.sql.Person;

public class CommandIdentifyUser {
	
    public static void ExecuteCommand(SlashCommandEvent event, User dc_user) {
        
        if (dc_user != null) {
        	Person searched = MySqlClient.get().searchPerson("", dc_user.getId(), "");
        	if (searched != null) {
                DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getIdentifyUserSuccess(dc_user, searched.getMinecraftName()));
                return;
        	}
        }

        DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordResponses.getIdentifyUserFail(dc_user));
        return;

    }
}