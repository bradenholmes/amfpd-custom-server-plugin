package uk.co.angrybee.joe.commands.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import uk.co.angrybee.joe.AuthorPermissions;
import uk.co.angrybee.joe.DiscordClient;
import uk.co.angrybee.joe.DiscordWhitelister;
import uk.co.angrybee.joe.sql.MySqlClient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandAdd {

    public static void ExecuteCommand(SlashCommandEvent event, String mc_user) {
        AuthorPermissions authorPermissions = new AuthorPermissions(event);
        User author = event.getUser();
        TextChannel channel = event.getTextChannel();
        Member member = event.getMember();

        int timesWhitelisted =0;
        final String finalNameToAdd = mc_user;
        final char[] finalNameToWhitelistChar = finalNameToAdd.toLowerCase().toCharArray(); // Lower case for char check
        boolean onlyHasLimitedAdd = false;
        if (DiscordClient.usernameValidation) {
            // Invalid char check
            for (char c : finalNameToWhitelistChar) {
                if (new String(DiscordClient.validCharacters).indexOf(c) == -1) {
                    EmbedBuilder embedBuilderInvalidChar;

                    if (!DiscordWhitelister.useCustomMessages) {
                        embedBuilderInvalidChar = DiscordClient.CreateEmbeddedMessage("Invalid Username", (author.getAsMention() + ", the username you have specified contains invalid characters. **Only letters, numbers and underscores are allowed**."), DiscordClient.EmbedMessageType.FAILURE);
                    } else {
                        String customTitle = DiscordWhitelister.getCustomMessagesConfig().getString("invalid-characters-warning-title");
                        String customMessage = DiscordWhitelister.getCustomMessagesConfig().getString("invalid-characters-warning");
                        customMessage = customMessage.replaceAll("\\{Sender}", author.getAsMention());

                        embedBuilderInvalidChar = DiscordClient.CreateEmbeddedMessage(customTitle, customMessage, DiscordClient.EmbedMessageType.FAILURE);
                    }
                    DiscordClient.ReplyAndRemoveAfterSeconds(event, embedBuilderInvalidChar.build());
                    return;
                }
            }

            // Length check
            if (finalNameToAdd.length() < 3 || finalNameToAdd.length() > 16) {
                EmbedBuilder embedBuilderLengthInvalid;

                if (!DiscordWhitelister.useCustomMessages) {
                    embedBuilderLengthInvalid = DiscordClient.CreateEmbeddedMessage("Invalid Username", (author.getAsMention() + ", the username you have specified either contains too few or too many characters. **Usernames can only consist of 3-16 characters**."), DiscordClient.EmbedMessageType.FAILURE);
                } else {
                    String customTitle = DiscordWhitelister.getCustomMessagesConfig().getString("invalid-length-warning-title");
                    String customMessage = DiscordWhitelister.getCustomMessagesConfig().getString("invalid-length-warning");
                    customMessage = customMessage.replaceAll("\\{Sender}", author.getAsMention());

                    embedBuilderLengthInvalid = DiscordClient.CreateEmbeddedMessage(customTitle, customMessage, DiscordClient.EmbedMessageType.FAILURE);
                }
                DiscordClient.ReplyAndRemoveAfterSeconds(event, embedBuilderLengthInvalid.build());
                return;
            }
        }

        if (authorPermissions.isUserCanAdd()) {


            // runs after member null check

            DiscordWhitelister.getPlugin().getLogger().info(author.getName() + "(" + author.getId() + ") attempted to whitelist: " + finalNameToAdd);

        } else {
            DiscordClient.ReplyAndRemoveAfterSeconds(event, DiscordClient.CreateInsufficientPermsMessage(author));
            return;
        }




        boolean alreadyOnWhitelist = false;

        if (WhitelistedPlayers.CheckForPlayer(finalNameToAdd)) {
            alreadyOnWhitelist = true;
        }

        if (alreadyOnWhitelist) {
            MessageEmbed messageEmbed =
                    DiscordClient.CreateEmbeddedMessage("User already on the whitelist",
                            (author.getAsMention() + ", cannot add user as `" + finalNameToAdd + "` is already on the whitelist!"), DiscordClient.EmbedMessageType.INFO).build();
            DiscordClient.ReplyAndRemoveAfterSeconds(event, messageEmbed);
            return;
        }


                /* Do as much as possible off the main server thread.
                convert username into UUID to avoid depreciation and rate limits (according to https://minotar.net/) */
        String playerUUID = DiscordClient.minecraftUsernameToUUID(finalNameToAdd);
        final boolean invalidMinecraftName = playerUUID == null;

                /* Configure success & failure messages here instead of on the main server thread -
                this will run even if the message is never sent, but is a good trade off */
        EmbedBuilder embedBuilderWhitelistSuccess;

        if (!DiscordWhitelister.useCustomMessages) {
            embedBuilderWhitelistSuccess = DiscordClient.CreateEmbeddedMessage((finalNameToAdd + " is now whitelisted!"), (author.getAsMention() + " has added `" + finalNameToAdd + "` to the whitelist."), DiscordClient.EmbedMessageType.SUCCESS);
        } else {
            String customTitle = DiscordWhitelister.getCustomMessagesConfig().getString("whitelist-success-title");
            customTitle = customTitle.replaceAll("\\{MinecraftUsername}", finalNameToAdd);

            String customMessage = DiscordWhitelister.getCustomMessagesConfig().getString("whitelist-success");
            customMessage = customMessage.replaceAll("\\{Sender}", author.getAsMention());
            customMessage = customMessage.replaceAll("\\{MinecraftUsername}", finalNameToAdd);

            embedBuilderWhitelistSuccess = DiscordClient.CreateEmbeddedMessage(customTitle, customMessage, DiscordClient.EmbedMessageType.SUCCESS);
        }


        if (DiscordWhitelister.showPlayerSkin) {
            if (!DiscordWhitelister.mainConfig.getFileConfiguration().getBoolean("use-crafatar-for-avatars"))
                embedBuilderWhitelistSuccess.setThumbnail("https://minotar.net/armor/bust/" + playerUUID + "/100.png");
            else
                embedBuilderWhitelistSuccess.setThumbnail("https://crafatar.com/avatars/" + playerUUID + "?size=100&default=MHF_Steve&overlay.png");
        }

        EmbedBuilder embedBuilderWhitelistFailure;

        if (!DiscordWhitelister.useCustomMessages) {
            embedBuilderWhitelistFailure = DiscordClient.CreateEmbeddedMessage("Failed to whitelist",
                    (author.getAsMention() + ", failed to add `" + finalNameToAdd + "` to the whitelist. This is most likely due to an invalid Minecraft username."), DiscordClient.EmbedMessageType.FAILURE);
        } else {
            String customTitle = DiscordWhitelister.getCustomMessagesConfig().getString("whitelist-failure-title");

            String customMessage = DiscordWhitelister.getCustomMessagesConfig().getString("whitelist-failure");
            customMessage = customMessage.replaceAll("\\{Sender}", author.getAsMention());
            customMessage = customMessage.replaceAll("\\{MinecraftUsername}", finalNameToAdd);

            embedBuilderWhitelistFailure = DiscordClient.CreateEmbeddedMessage(customTitle, customMessage, DiscordClient.EmbedMessageType.FAILURE);
        }


        AtomicBoolean successfulWhitelist = new AtomicBoolean(false);

        if (authorPermissions.isUserCanAdd()) {
            DiscordClient.ExecuteServerCommand("whitelist add " + finalNameToAdd);
            MySqlClient.get().logWhitelistEvent(author.getId(), "ADD", finalNameToAdd);
        }

        DiscordWhitelister.getPlugin().getServer().getScheduler().callSyncMethod(DiscordWhitelister.getPlugin(), () ->
        {
            if (WhitelistedPlayers.usingEasyWhitelist && !invalidMinecraftName && WhitelistedPlayers.CheckForPlayerEasyWhitelist(finalNameToAdd)
                    || !WhitelistedPlayers.usingEasyWhitelist && WhitelistedPlayers.CheckForPlayer(finalNameToAdd)) {
                event.replyEmbeds(embedBuilderWhitelistSuccess.build()).queue();

                // For instructional message
                successfulWhitelist.set(true);
                
                


            } else {
                DiscordClient.ReplyAndRemoveAfterSeconds(event, embedBuilderWhitelistFailure.build());
            }
            return null;
        });
    }

    private static boolean checkMcUsername(String nameToCheck) {
        if (DiscordClient.usernameValidation) {
            // Length check
            if (nameToCheck.length() < 3 || nameToCheck.length() > 16) {
                return false;
            }
            // Invalid char check
            for (char c : nameToCheck.toCharArray()) {
                if (new String(DiscordClient.validCharacters).indexOf(c) == -1) {
                    return false;
                }
            }
        }
        return true;
    }
}