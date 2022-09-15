package uk.co.angrybee.joe.events;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import uk.co.angrybee.joe.sql.MySqlClient;
import uk.co.angrybee.joe.sql.Person;
import uk.co.angrybee.joe.DiscordClient;
import uk.co.angrybee.joe.DiscordWhitelister;

import java.io.IOException;


public class OnBanEvent implements Listener
{
    @EventHandler
    public void onCommandBan(PlayerCommandPreprocessEvent e) throws IOException {
        Player commandCaller = e.getPlayer();
        if(commandCaller.hasPermission("minecraft.command.ban")) {
        	handle(e.getPlayer().getName(), "/ban", e.getMessage().toLowerCase());
        }
    }
	
    @EventHandler
    public void onCommandBan(ServerCommandEvent e) throws IOException {
        handle("Server Console", "ban", e.getCommand().toLowerCase());
    }
    
    
    private void handle(String caller, String cmdPrefix, String command) {
        if(!command.startsWith(cmdPrefix)) {
            return;
        }


        String banTarget = command.substring(cmdPrefix.length() + 1).toLowerCase();
        // Remove ban reason if there is one
        if(banTarget.contains(" ")) {
            banTarget = banTarget.substring(0, banTarget.indexOf(" "));
        }

        if(StringUtils.isEmpty(banTarget)) {
        	return;
        }
        
        DiscordWhitelister.getPluginLogger().info(caller + " has banned player '" + banTarget + "'");
        
        Person banned = MySqlClient.get().searchPerson(banTarget, "", "");
        if (banned == null) {
        	return;
        }
        
        if (banned.isWhitelisted()) {
        	DiscordWhitelister.ExecuteServerCommand("whitelist remove " + banTarget);
        }
        
        MySqlClient.get().updatePerson(banned.getPrimaryId(), "", "", "", false, true);
        

        if (banned.getDiscordId() != 0) {
        	DiscordClient.RemoveRoleFromUser(String.valueOf(banned.getDiscordId()), DiscordWhitelister.mainConfig.getFileConfiguration().getString("member-role"));
        	DiscordClient.AssignRoleToUser(String.valueOf(banned.getDiscordId()), DiscordWhitelister.mainConfig.getFileConfiguration().getString("banned-role"));
        } else {
        	DiscordWhitelister.getPluginLogger().warning(banTarget + " does not have a linked Discord Id, cannot assign roles!");
        }
    }
}
