package uk.co.angrybee.joe.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import uk.co.angrybee.joe.DiscordWhitelister;

import java.io.File;
import java.util.Arrays;


// discord-whitelister.yml
public class MainConfig extends Config {
    public static String default_token = "Discord bot token goes here, you can find it here: https://discordapp.com/developers/applications/";
    public MainConfig() {
        fileName = "discord-whitelister.yml";
        file = new File(DiscordWhitelister.getPlugin().getDataFolder(), fileName);
        fileConfiguration = new YamlConfiguration();
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public boolean fileCreated = false;

    public void ConfigSetup() {


        // Create root folder for configs if it does not exist
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        if (!file.exists()) {
            CreateConfig();
            DiscordWhitelister.getPluginLogger().warning("Configuration file created at: " + file.getPath() +
                    ", please edit this or the plugin will not work!");
        }
        LoadConfigFile();
        CheckEntries();
        SaveConfig();
    }


    private void CheckEntries() {
        CheckEntry("bot-enabled", true);

        CheckEntry("discord-bot-token",
                default_token);
        
        CheckEntry("guild-id", "1016931782194626601");
        CheckEntry("member-role", "1017895030649794622");
        CheckEntry("banned-role", "1019703075256147988");
        // Allowed to add to the whitelist
        CheckEntry("add-roles", Arrays.asList("1017895030649794622", "1017942234252968018"));
        
        // Allowed to remove from the whitelist
        CheckEntry("remove-roles", Arrays.asList("1017885949532835883", "1017883178381348945"));

        CheckEntry("target-text-channels", Arrays.asList("1017923462301503549"));

        CheckEntry("remove-unnecessary-messages-from-whitelist-channel", false);
        CheckEntry("seconds-to-remove-message-from-whitelist-channel", 5);
        
        CheckEntry("deathpunish-enabled", true);
        CheckEntry("deathpunish-punish-pvp", false);
        CheckEntry("deathpunish-timeout-enabled", true);
        CheckEntry("deathpunish-timeout-duration", 90);
        CheckEntry("deathpunish-xp-multiplyby", 0.0);
        CheckEntry("deathpunish-xp-drop", true);
        CheckEntry("deathpunish-inv-stackable-multiplyby", 0.5);
        CheckEntry("deathpunish-inv-unstackable-destroy", false);
        CheckEntry("deathpunish-inv-equipment-durability-multiplyby", 0.9);
        CheckEntry("deathpunish-inv-drop", true);
    }
}
