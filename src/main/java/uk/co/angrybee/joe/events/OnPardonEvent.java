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


public class OnPardonEvent implements Listener
{
    @EventHandler
    public void onCommandPardon(PlayerCommandPreprocessEvent e) throws IOException
    {
        Player commandCaller = e.getPlayer();
        if(commandCaller.hasPermission("minecraft.command.pardon")) {
        	handle(commandCaller.getName(), "/pardon", e.getMessage().toLowerCase());
        }
    }
    
    @EventHandler
    public void onCommandPardon(ServerCommandEvent e) throws IOException {
        handle("Server Console", "pardon", e.getCommand().toLowerCase());
    }
    
    private void handle(String caller, String cmdPrefix, String command) {
        if(!command.startsWith(cmdPrefix)) {
        	return;
        }
            
        String pardonTarget = command.substring(cmdPrefix.length() + 1).toLowerCase();

        
        if(pardonTarget.contains(" ")) {
            pardonTarget = pardonTarget.substring(0, pardonTarget.indexOf(" "));
        }

        if(StringUtils.isEmpty(pardonTarget)) {
        	return;
        }
        
        DiscordWhitelister.getPluginLogger().info(caller + " has unbanned player '" + pardonTarget + "'");
        
        Person pardoned = MySqlClient.get().searchPerson(pardonTarget, "", "");
        if (pardoned == null) {
        	return;
        }
        
        MySqlClient.get().updatePerson(pardoned.getPrimaryId(), "", "", "", true, false);
        
        
        DiscordWhitelister.ExecuteServerCommand("whitelist add " + pardonTarget);
        if (pardoned.getDiscordId() != 0) {
        	DiscordClient.RemoveRoleFromUser(String.valueOf(pardoned.getDiscordId()), DiscordWhitelister.mainConfig.getFileConfiguration().getString("banned-role"));
        	DiscordClient.AssignRoleToUser(String.valueOf(pardoned.getDiscordId()), DiscordWhitelister.mainConfig.getFileConfiguration().getString("member-role"));
        	
        } else {
        	DiscordWhitelister.getPluginLogger().warning(pardonTarget + " does not have a linked Discord Id, cannot assign roles!");
        }
    }
}
