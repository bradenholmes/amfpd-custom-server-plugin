package uk.co.angrybee.joe;

import org.bukkit.plugin.java.JavaPlugin;
import uk.co.angrybee.joe.commands.minecraft.CommandDiscord;
import uk.co.angrybee.joe.configs.*;
import uk.co.angrybee.joe.events.OnBanEvent;
import uk.co.angrybee.joe.events.OnJoinEvent;
import uk.co.angrybee.joe.events.OnPardonEvent;
import uk.co.angrybee.joe.events.OnWhitelistEvent;
import uk.co.angrybee.joe.sql.MySqlClient;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class DiscordWhitelister extends JavaPlugin {
    public static String botToken;

    private static boolean configCreated = false;

    public static boolean initialized = false;
    public static boolean showPlayerSkin = true;

    public static boolean removeUnnecessaryMessages = false;

    public static boolean botEnabled;


    private static JavaPlugin thisPlugin;
    private static Logger pluginLogger;


    public static int removeMessageWaitTime = 5;

    public static MainConfig mainConfig;


    @Override
    public void onEnable() {
        thisPlugin = this;
        pluginLogger = thisPlugin.getLogger();


        int initSuccess = InitBot(true);

        if (initSuccess == 0) {
            pluginLogger.info("Successfully initialized Discord client");
            initialized = true;
        } else if (initSuccess == 1) {
            pluginLogger.severe("Discord Client failed to initialize, please check if your config file is valid");
            initialized = false;
            return;
        }

        this.getCommand("discord").setExecutor(new CommandDiscord());
        
        MySqlClient.get();
        pluginLogger.info("Successfully established MySql connection");
    }

    @Override
    public void onDisable() {
        if (initialized ) {
            DiscordClient.javaDiscordAPI.shutdownNow();
        }
        
        try {
			MySqlClient.get().closeConnection();
			pluginLogger.info("Closed MySQL connection");
		} catch (SQLException e) {
			pluginLogger.severe("Failed to close MySQL connection");
		}
        
    }

    public static JavaPlugin getPlugin() {
        return thisPlugin;
    }


    public static Logger getPluginLogger() {
        return pluginLogger;
    }

    static String[] getConfigArray(String path){
        List<String> list = mainConfig.getFileConfiguration().getStringList(path);
        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }

    public static int getMaximumAllowedPlayers() {
        return thisPlugin.getServer().getMaxPlayers();
    }

    public static int InitBot(boolean firstInit) {

        ConfigSetup();

        botToken = mainConfig.getFileConfiguration().getString("discord-bot-token");
        botEnabled = mainConfig.getFileConfiguration().getBoolean("bot-enabled");
        showPlayerSkin = mainConfig.getFileConfiguration().getBoolean("show-player-skin-on-whitelist");
        configCreated = mainConfig.fileCreated;
        removeUnnecessaryMessages = mainConfig.getFileConfiguration().getBoolean("remove-unnecessary-messages-from-whitelist-channel");
        removeMessageWaitTime = mainConfig.getFileConfiguration().getInt("seconds-to-remove-message-from-whitelist-channel");

        if (!botEnabled) {
            pluginLogger.info("Bot is disabled as per the config, doing nothing");
        } else if (configCreated || botToken.equals(MainConfig.default_token)) {
            pluginLogger.warning("Config newly created. Please paste your bot token into the config file, doing nothing until next server start");
        } else {

            pluginLogger.info("Initializing Discord client...");


            // set add roles
            DiscordClient.allowedToAddRoles = getConfigArray("add-roles");
            
            
            // set add & remove roles
            DiscordClient.allowedToRemoveRoles = getConfigArray("remove-roles");
            
            thisPlugin.getServer().getPluginManager().registerEvents(new OnBanEvent(), thisPlugin);
            thisPlugin.getServer().getPluginManager().registerEvents(new OnPardonEvent(), thisPlugin);
            thisPlugin.getServer().getPluginManager().registerEvents(new OnWhitelistEvent(), thisPlugin);
            thisPlugin.getServer().getPluginManager().registerEvents(new OnJoinEvent(), thisPlugin);


            int initSuccess = DiscordClient.InitializeClient(botToken);

            if (initSuccess == 1) {
                return 1;
            }

            return 0;
        }

        return 1;
    }

    public static void ConfigSetup() {
        mainConfig = new MainConfig();
        mainConfig.ConfigSetup();
    }
    
    public static void ExecuteServerCommand(String command) {
        DiscordWhitelister.getPlugin().getServer().getScheduler().callSyncMethod(DiscordWhitelister.getPlugin(), ()
                -> DiscordWhitelister.getPlugin().getServer().dispatchCommand(
                DiscordWhitelister.getPlugin().getServer().getConsoleSender(), command));
    }
}
