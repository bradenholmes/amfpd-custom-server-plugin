package uk.co.angrybee.joe.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import uk.co.angrybee.joe.DiscordWhitelister;

public class OnAnvilEvent implements Listener
{

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onAnvilPrepare(PrepareAnvilEvent e) {
    	
    	if (e.getResult() == null) {
    		return;
    	}
    	
    	if (!DiscordWhitelister.mainConfig.getFileConfiguration().getBoolean("infinitely-repair-tools")) {
    		return;
    	}
    	
    	ItemStack firstItem = e.getInventory().getItem(0);
    	ItemStack secondItem = e.getInventory().getItem(1);
    	if (firstItem != null && firstItem.getItemMeta() instanceof Repairable) {
    		int repairCost = ((Repairable) firstItem.getItemMeta()).getRepairCost();
    		if (secondItem != null && isRepairMaterial(firstItem.getType(), secondItem.getType())) {
    			
    			if (!secondItem.getItemMeta().hasEnchants()) {
    	    		ItemStack result = e.getResult();
    	    		Repairable resultMeta = (Repairable) result.getItemMeta();
    	    		resultMeta.setRepairCost(repairCost);
    	    		result.setItemMeta(resultMeta);
    	    		e.setResult(result);
    			}

    		}
    	}
    	
    }
    
    private boolean isRepairMaterial(Material m1, Material m2) {
    	if (m2 == Material.COBBLESTONE || m2 == Material.IRON_INGOT || m2 == Material.GOLD_INGOT || m2 == Material.DIAMOND || m2 == Material.NETHERITE_INGOT) {
    		return true;
    	}
    	
    	if (m1 == m2) {
    		return true;
    	}
    	
    	return false;
    }
    
}
