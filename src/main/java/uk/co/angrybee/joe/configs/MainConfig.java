package uk.co.angrybee.joe.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import uk.co.angrybee.joe.DiscordWhitelister;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;


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
        

        // Allowed to add to the whitelist
        CheckEntry("add-roles", Arrays.asList("Member", "Whitelister"));
        
        // Allowed to remove from the whitelist
        CheckEntry("remove-roles", Arrays.asList("Admin", "Moderator"));

        CheckEntry("target-text-channels", Arrays.asList("000000000000000000", "111111111111111111"));


        CheckEntry("show-player-skin-on-whitelist", true);
        CheckEntry("send-instructional-message-on-whitelist", false);

        CheckEntry("remove-unnecessary-messages-from-whitelist-channel", false);
        CheckEntry("seconds-to-remove-message-from-whitelist-channel", 5);
        
        CheckEntry("username-validation", true);


        // Remove old role entry if found, move role to new array (for people with v1.3.6 or below)
        if (fileConfiguration.get("whitelisted-role") != null) {
            DiscordWhitelister.getPluginLogger().warning("Found whitelisted-role entry, moving over to whitelisted-roles. Please check your config to make sure the change is correct");
            // Get the role from the old entry
            String whitelistedRoleTemp = fileConfiguration.getString("whitelisted-role");
            // Assign role from old entry to new entry as a list
            fileConfiguration.set("whitelisted-roles", Collections.singletonList(whitelistedRoleTemp));

            // Remove now un-used entry
            fileConfiguration.set("whitelisted-role", null);

            // Note to users that id for roles now affects the new entry
            if (fileConfiguration.getBoolean("use-id-for-roles")) {
                DiscordWhitelister.getPluginLogger().severe("You have 'use-id-for-roles' enabled please change the whitelisted-roles to ids as they now follow this setting");
            }
        }
    }
}
