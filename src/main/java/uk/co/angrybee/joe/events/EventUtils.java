package uk.co.angrybee.joe.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EventUtils
{
	
	
	
	
    public static void setTotalExperience(@NotNull Player player, int amount) {
        amount = Math.max(0, amount);

        int level = 0;
        int xp = 0;
        float a;
        float b;
        float c = -amount;

        if (amount > getTotalExperience(0) && amount <= getTotalExperience(15)) {
            a = 1;
            b = 6;
        } else if (amount > getTotalExperience(15) && amount <= getTotalExperience(30)) {
            a = 2.5f;
            b = -40.5f;
            c += 360;
        } else {
            a = 4.5f;
            b = -162.5f;
            c += 2220;
        }
        level = (int) Math.floor((-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a));
        xp = amount - getTotalExperience(level);
        player.setLevel(level);
        player.setExp(0);
        player.giveExp(xp);
    }
    
    /**
     * Get total current experience of player
     * @param player
     * @return
     */
    public static int getTotalExperience(@NotNull Player player) {
        return Math.round(player.getExp() * player.getExpToLevel()) + getTotalExperience(player.getLevel());
    }
    
    /**
     * Get total experience of level
     * @param level
     * @return
     */
    public static int getTotalExperience(int level) {
        int xp = 0;

        if (level >= 0 && level <= 15) {
            xp = (int) Math.round(Math.pow(level, 2) + 6 * level);
        } else if (level > 15 && level <= 30) {
            xp = (int) Math.round((2.5 * Math.pow(level, 2) - 40.5 * level + 360));
        } else if (level > 30) {
            xp = (int) Math.round((4.5 * Math.pow(level, 2) - 162.5 * level + 2220));
        }
        return xp;
    }
    
    public static void spawnOrbs(int xp, World world, Location loc) {
    	
    	int orbSize = Math.max(50, xp / 10);
        while(xp > 0) {
            ExperienceOrb orb = (ExperienceOrb) world.spawnEntity(loc, EntityType.EXPERIENCE_ORB);
            orb.setExperience(Math.min(orbSize, xp));
            xp = xp - orbSize;
        }
    }
}
