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
import uk.co.angrybee.joe.Utils.WhitelistEventType;

import java.io.IOException;


public class OnWhitelistEvent implements Listener
{
	private static final int RC_SUCCESS = 0;
	private static final int RC_WRONG_CMD = 1;
	private static final int RC_BAD_CMD = 2;
	private static final int RC_BANNED = 3;
	
    @EventHandler
    public void onCommandAdd(PlayerCommandPreprocessEvent e) throws IOException {
        Player commandCaller = e.getPlayer();
        if(commandCaller.hasPermission("minecraft.command.whitelist.add")) {
        	
        	int rc = handleAdd(e.getPlayer().getName(), "/whitelist add", e.getMessage().toLowerCase());
            if (rc == RC_BANNED) {
            	DiscordWhitelister.ExecuteServerCommand("tell " + commandCaller.getName() + " cannot add banned players to the whitelist!");
            }
        }
    }
	
    @EventHandler
    public void onCommandAdd(ServerCommandEvent e) throws IOException {
        int rc = handleAdd("Server Console", "whitelist add", e.getCommand().toLowerCase());
        if (rc == RC_BANNED) {
        	DiscordWhitelister.getPluginLogger().severe("cannot add banned players to the whitelist!");
        }
    }
    
    
    private int handleAdd(String caller, String cmdPrefix, String command) {
        if(!command.startsWith(cmdPrefix)) {
            return RC_WRONG_CMD;
        }


        String target = command.substring(cmdPrefix.length() + 1).toLowerCase();
        // Remove ban reason if there is one
        if(target.contains(" ")) {
        	target = target.substring(0, target.indexOf(" "));
        }

        if(StringUtils.isEmpty(target)) {
        	return RC_BAD_CMD;
        }
        
        String targetId = Utils.minecraftUsernameToUUID(target);
        Person targetPerson = MySqlClient.searchPerson(targetId, "", "", "");
        if (targetPerson == null) {
        	targetPerson = MySqlClient.insertPerson(targetId, target, "", "", false, false);
        }
        
        if (targetPerson.isBanned()) {
        	return RC_BANNED;
        }
        
        targetPerson.setWhitelisted(true);
        
        MySqlClient.updatePerson(targetPerson);
        
        Person callerPerson = MySqlClient.searchPerson(Utils.minecraftUsernameToUUID(caller), "", "", "");
        if (callerPerson != null) {
        	MySqlClient.logWhitelistEvent(callerPerson.getPrimaryId(), WhitelistEventType.ADD, targetPerson.getPrimaryId());
        }
        return RC_SUCCESS;
    }
    
    @EventHandler
    public void onCommandRemove(PlayerCommandPreprocessEvent e) throws IOException {
        Player commandCaller = e.getPlayer();
        if(commandCaller.hasPermission("minecraft.command.whitelist.remove")) {
        	handleRemove(e.getPlayer().getName(), "/whitelist remove", e.getMessage().toLowerCase());
        }
    }
	
    @EventHandler
    public void onCommandRemove(ServerCommandEvent e) throws IOException {
        handleRemove("Server Console", "whitelist remove", e.getCommand().toLowerCase());
    }
    
    private void handleRemove(String caller, String cmdPrefix, String command) {
        if(!command.startsWith(cmdPrefix)) {
            return;
        }

        String target = command.substring(cmdPrefix.length() + 1).toLowerCase();
        
        // Remove ban reason if there is one
        if(target.contains(" ")) {
        	target = target.substring(0, target.indexOf(" "));
        }

        if(StringUtils.isEmpty(target)) {
        	return;
        }
        
        String targetId = Utils.minecraftUsernameToUUID(target);
        Person targetPerson = MySqlClient.searchPerson(targetId, "", "", "");
        if (targetPerson != null) {
            Person callerPerson = MySqlClient.searchPerson(Utils.minecraftUsernameToUUID(caller), "", "", "");
            if (callerPerson != null) {
            	MySqlClient.logWhitelistEvent(callerPerson.getPrimaryId(), WhitelistEventType.REMOVE, targetPerson.getPrimaryId());
            }
            
            targetPerson.setWhitelisted(false);
            
        	MySqlClient.updatePerson(targetPerson);
        	return;
        }
        
        
    }
}
