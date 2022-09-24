package uk.co.angrybee.joe.events;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import uk.co.angrybee.joe.DiscordWhitelister;
import uk.co.angrybee.joe.sql.DeathBan;
import uk.co.angrybee.joe.sql.MySqlClient;

public class OnPlayerDeathEvent implements Listener
{
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        
        //World gameRule keepInventory must be set to TRUE for this system to function
        if (!player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) {
        	DiscordWhitelister.getPluginLogger().severe("DEATH PUNISH is not enabled because gamerule keepInventory=false");
        	return;
        }
        
        //If the DEATH_PUNISH system is ENABLED
        if (DiscordWhitelister.mainConfig.getFileConfiguration().getBoolean("deathpunish-enabled")) {
        	
        	//If player has a killer (another player) AND config doesn't punish, return
        	if (player.getKiller() != null && !DiscordWhitelister.mainConfig.getFileConfiguration().getBoolean("deathpunish-punish-pvp")) {
        		return;
        	}


        	handleExperience(player);
        	handleInventory(player);
        	
        	MySqlClient.logPlayerDeath(player.getUniqueId().toString());
        	
        	if (DiscordWhitelister.mainConfig.getFileConfiguration().getBoolean("deathpunish-timeout-enabled")) {
        		String cause = StringUtils.substringAfter(e.getDeathMessage(), " ");
        		cause = StringUtils.replaceAll(cause, "was", "were");
        		
        		StringBuilder sb = new StringBuilder();
        		sb.append("§cYou ");
        		sb.append(cause);
        		sb.append("....");
        		
        		
        		int timeoutTime = DiscordWhitelister.mainConfig.getFileConfiguration().getInt("deathpunish-timeout-duration");
        		player.kickPlayer(sb.toString() + "\n\n§fand have been §etimed out §ffor §b" + timeoutTime + " §fseconds!");
        		MySqlClient.insertDeathBan(player.getUniqueId().toString());
        	}
        }
    }
    
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
    	Player player = e.getPlayer();
    	DeathBan deathBan = MySqlClient.getDeathBan(player.getUniqueId().toString());
    	if (deathBan != null) {
    		long timeSince = System.currentTimeMillis() - deathBan.getTime().getTime();
    		long secondsSince = timeSince / 1000;
    		int timeoutTime = DiscordWhitelister.mainConfig.getFileConfiguration().getInt("deathpunish-timeout-duration");
    		if (secondsSince < timeoutTime) {
    			e.disallow(Result.KICK_OTHER, "you are timed out for §b" + (timeoutTime - secondsSince) + "§f more seconds....");
    		} else {
    			MySqlClient.clearDeathBan(player.getUniqueId().toString());
    		}
    		
    	}
    }
    
    /**
     * Multiply player XP by config's specifications
     */
    private static void handleExperience(Player player) {
    	int currentXp = EventUtils.getTotalExperience(player);
    	int newXp = (int) Math.round(currentXp * DiscordWhitelister.mainConfig.getFileConfiguration().getDouble("deathpunish-xp-multiplyby"));
    	
    	
    	
    	if (DiscordWhitelister.mainConfig.getFileConfiguration().getBoolean("deathpunish-xp-drop")) {
    		EventUtils.setTotalExperience(player, 0);
            EventUtils.spawnOrbs(newXp, player.getWorld(), player.getLocation());
    	} else {
    		EventUtils.setTotalExperience(player, newXp);
    	}
    }
    
    private static void handleInventory(Player player) {
    	ItemStack[] inv = player.getInventory().getContents();
    	
    	boolean dropOnGround = DiscordWhitelister.mainConfig.getFileConfiguration().getBoolean("deathpunish-inv-drop");
    	
    	for (int i = 0; i < inv.length; i++) {
    		ItemStack item = inv[i];
    		if (item == null) {
    			continue;
    		}
    		
    		//Unstackables
    		if (item.getMaxStackSize() == 1) {
    		
    			//if config says to destroy unstackables, do so and continue
    			if (DiscordWhitelister.mainConfig.getFileConfiguration().getBoolean("deathpunish-inv-unstackable-destroy")) {
    				inv[i] = null;
    				continue;
    			}
    			
    			//otherwise multiply durability by config specs
    			inv[i] = multiplyItemDurability(item, DiscordWhitelister.mainConfig.getFileConfiguration().getDouble("deathpunish-inv-equipment-durability-multiplyby"));
    		
    		//Stackables
    		} else {
    			
    			//multiply by config specs
    			int newAmount = (int) Math.round(item.getAmount() * DiscordWhitelister.mainConfig.getFileConfiguration().getDouble("deathpunish-inv-stackable-multiplyby"));
    			item.setAmount(newAmount);
    		}
    		
    		//Drop items if true
            if (dropOnGround) {
                player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                inv[i] = null;
            }
    	}
    	
    	
    	
    	
    	
    	player.getInventory().setContents(inv);
    }
    
    private static ItemStack multiplyItemDurability(ItemStack itemStack, double multiplier) {
    	if (itemStack == null) {
    		return null;
    	}
    	
    	ItemMeta meta = itemStack.getItemMeta();
    	if (!(meta instanceof Damageable)) {
    		return itemStack;
    	}
    	
    	Damageable dMeta = (Damageable) meta;
    	
    	int maxDurability = itemStack.getType().getMaxDurability();
    	int curDurability = maxDurability - dMeta.getDamage();
    	int newDurability = (int) Math.round(curDurability * multiplier);
    	
    	if (newDurability > maxDurability) {
    		newDurability = maxDurability;
    	} else if (newDurability < 1) {
    		newDurability = 1;
    	}
    	
    	dMeta.setDamage(maxDurability - newDurability);
    	itemStack.setItemMeta((ItemMeta) dMeta);
    	return itemStack;
    }
}
