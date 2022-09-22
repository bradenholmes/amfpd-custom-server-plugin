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
import uk.co.angrybee.joe.Utils;

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
            
        String targetName = command.substring(cmdPrefix.length() + 1).toLowerCase();

        
        if(targetName.contains(" ")) {
            targetName = targetName.substring(0, targetName.indexOf(" "));
        }

        if(StringUtils.isEmpty(targetName)) {
        	return;
        }
        
        String targetId = Utils.minecraftUsernameToUUID(targetName);
        
        DiscordWhitelister.getPluginLogger().info(caller + " has unbanned player '" + targetName + "'");
        
        Person pardoned = MySqlClient.searchPerson(targetId, "", "", "");
        if (pardoned == null) {
        	return;
        }
        
        pardoned.setWhitelisted(true);
        pardoned.setBanned(false);
        
        MySqlClient.updatePerson(pardoned);
        
        
        DiscordWhitelister.ExecuteServerCommand("whitelist add " + targetName);
        if (pardoned.getDiscordId() != 0) {
        	DiscordClient.RemoveRoleFromUser(String.valueOf(pardoned.getDiscordId()), DiscordWhitelister.mainConfig.getFileConfiguration().getString("banned-role"));
        	DiscordClient.AssignRoleToUser(String.valueOf(pardoned.getDiscordId()), DiscordWhitelister.mainConfig.getFileConfiguration().getString("member-role"));
        	
        } else {
        	DiscordWhitelister.getPluginLogger().warning(targetName + " does not have a linked Discord Id, cannot assign roles!");
        }
    }
}
