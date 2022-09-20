package uk.co.angrybee.joe.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.clip.placeholderapi.PlaceholderAPI;

public class OnJoinEvent implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		
		int level = Integer.valueOf(PlaceholderAPI.setPlaceholders(event.getPlayer(), "%smptweaks_level%"));
		
		String levelString;
		if (level < 10) {
			levelString = " " + level;
		} else {
			levelString = "" + level;
		}
		
		String levelColor;
		if (level < 10) {
			levelColor = "§7";
		} else if (level < 20) {
			levelColor = "§e";
		} else if (level < 30) {
			levelColor = "§2";
		} else if (level < 40) {
			levelColor = "§9";
		} else if (level < 50) {
			levelColor = "§c";
		} else {
			levelColor = "§5";
		}
		
		
		String nameString = levelColor + "[Lv." + levelString + "]§f " + event.getPlayer().getName();
		
		event.getPlayer().setDisplayName(nameString);
	}
}
