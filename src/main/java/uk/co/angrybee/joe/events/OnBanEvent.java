package uk.co.angrybee.joe.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import uk.co.angrybee.joe.sql.MySqlClient;
import uk.co.angrybee.joe.sql.Person;
import uk.co.angrybee.joe.DiscordClient;
import uk.co.angrybee.joe.DiscordWhitelister;

import java.io.IOException;


public class OnBanEvent implements Listener
{
    @EventHandler
    public void onCommandBan(PlayerCommandPreprocessEvent e) throws IOException
    {
    	DiscordWhitelister.getPluginLogger().info("ENTERED ON BAN EVENT");
        // Context
        Player commandCaller = e.getPlayer();
        String message = e.getMessage().toLowerCase();

        // Check if player is using the ban command
        if(!message.startsWith("/ban"))
            return;

        // Check if the player has permission to use the ban command
        if(!commandCaller.hasPermission("bukkit.command.ban.player"))
            return;

        String banTarget = message.substring("/ban".length() + 1).toLowerCase();
        // Remove ban reason if there is one
        if(banTarget.contains(" "))
        {
            banTarget = banTarget.substring(0, banTarget.indexOf(" "));
        }

        // Check if there is a name to query
        if(banTarget.equals(""))
            return;
        
        DiscordWhitelister.getPluginLogger().info(commandCaller.getName() + " has banned player '" + banTarget + "' in-game.");
        
        Person banned = MySqlClient.get().searchPerson(banTarget, "", "");
        if (banned == null || !banned.isWhitelisted()) {
        	return;
        }
        
        
        DiscordWhitelister.ExecuteServerCommand("whitelist remove " + banTarget);
        if (banned.getDiscordId() != 0) {
        	DiscordClient.RemoveRoleFromUser(String.valueOf(banned.getDiscordId()), DiscordWhitelister.mainConfig.getFileConfiguration().getString("member-role"));
        	DiscordClient.AssignRoleToUser(String.valueOf(banned.getDiscordId()), DiscordWhitelister.mainConfig.getFileConfiguration().getString("banned-role"));
        } else {
        	DiscordWhitelister.getPluginLogger().warning(banTarget + " does not have a linked Discord Id, cannot assign roles!");
        }
    }
}
