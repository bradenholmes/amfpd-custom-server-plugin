package uk.co.angrybee.joe.commands.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import uk.co.angrybee.joe.DiscordClient;
import uk.co.angrybee.joe.sql.MySqlClient;
import uk.co.angrybee.joe.sql.Person;

public class CommandIdentifyUser {
	
    public static void ExecuteCommand(SlashCommandEvent event, long dc_id) {
        
        if (dc_id != 0) {
        	Person searched = MySqlClient.get().searchPerson("", String.valueOf(dc_id), "");
        	if (searched != null) {
                EmbedBuilder insertedMessage = DiscordClient.CreateEmbeddedMessage("Found!", (searched.getDiscordName() + "'s in-game name is '" + searched.getMinecraftName() + "'"),
                        DiscordClient.EmbedMessageType.SUCCESS);
                DiscordClient.ReplyAndRemoveAfterSeconds(event, insertedMessage.build());
                return;
        	}
        }

        EmbedBuilder insertedMessage = DiscordClient.CreateEmbeddedMessage("No user found!", (""),
                DiscordClient.EmbedMessageType.FAILURE);
        DiscordClient.ReplyAndRemoveAfterSeconds(event, insertedMessage.build());
        return;

    }
}