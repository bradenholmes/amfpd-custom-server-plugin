package uk.co.angrybee.joe;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import uk.co.angrybee.joe.Utils.UsernameValidation;

public class DiscordResponses
{
    public enum EmbedMessageType {INFO, SUCCESS, FAILURE}

    private static final Color infoColor = new Color(104, 109, 224);
    private static final Color successColor = new Color(46, 204, 113);
    private static final Color failureColor = new Color(231, 76, 60);

    
    public static MessageEmbed getWhitelistAddSuccess(User author, String mc_username) {
    	
    	EmbedBuilder eb = new EmbedBuilder();
    	eb.setTitle("Success!");
    	eb.setDescription(mc_username + " is now whitelisted!");
    	eb.setThumbnail("https://minotar.net/armor/bust/" + Utils.minecraftUsernameToUUID(mc_username) + "/100.png");
    	eb.addField("added by:", author.getAsMention(), false);
    	
    	eb.setColor(successColor);

    	return eb.build();
    }
    
    public static MessageEmbed getWhitelistRemoveSuccess(User author, String mc_username) {
    	
    	EmbedBuilder eb = new EmbedBuilder();
    	eb.setTitle("Success!");
    	eb.setDescription(mc_username + " has been removed from the whitelist!");
    	eb.setThumbnail("https://minotar.net/armor/bust/" + Utils.minecraftUsernameToUUID(mc_username) + "/100.png");
    	eb.addField("removed by:", author.getAsMention(), false);
    	
    	eb.setColor(successColor);

    	return eb.build();
    }
    
    public static MessageEmbed getIdentifySelfSuccess(User author, String mc_username) {
    	
    	EmbedBuilder eb = new EmbedBuilder();
    	eb.setTitle("Thanks!");
    	eb.setDescription("you have identified yourself as '" + mc_username + "'. If this is an error, contact an administrator");
    	eb.setThumbnail("https://minotar.net/armor/bust/" + Utils.minecraftUsernameToUUID(mc_username) + "/100.png");
    	
    	eb.setColor(successColor);

    	return eb.build();
    }
    
    public static MessageEmbed getIdentifyUserSuccess(User dc_user, String mc_username) {
    	EmbedBuilder eb = new EmbedBuilder();
    	
    	eb.addField("Found!", "Discord user " + dc_user.getAsMention() + " is minecraft player '" + mc_username + "'", false);
    	eb.setThumbnail("https://minotar.net/armor/bust/" + Utils.minecraftUsernameToUUID(mc_username) + "/100.png");
    	eb.setColor(successColor);
    	
    	return eb.build();
    }
    
    public static MessageEmbed getIdentifyUserFail(User dc_user) {
    	EmbedBuilder eb = new EmbedBuilder();
    	
    	if (dc_user == null) {
    		eb.addField("Not Found!", "couldn't find that discord user on this server", false);
    	} else {
    		eb.addField("Not Found!", "discord user " + dc_user.getAsMention() + " does not have minecraft username linked to it", false);
    	}
    	eb.setColor(failureColor);
    	
    	return eb.build();
    }
    
    public static MessageEmbed getInvalidUsername(User messageAuthor, String mc_username, UsernameValidation validationResult) {
    	EmbedBuilder eb = new EmbedBuilder();
        //MessageEmbed invalidUsernameEmbed;

    	eb.setTitle("Invalid Minecraft Username");
    	eb.setDescription(messageAuthor.getAsMention() + ", the minecraft username you entered (" + mc_username + ") is invalid!");
    	
        if (validationResult == UsernameValidation.LENGTH_FAIL) {
        	eb.addField("reason:", "mc usernames can only contain 3-16 characters", false);
        } else if (validationResult == UsernameValidation.CHAR_FAIL) {
        	eb.addField("reason:", "mc usernames cannot contain special characters. only letters, numbers, and underscores are allowed", false);
        } else if (validationResult == UsernameValidation.DNE_FAIL) {
        	eb.addField("reason:", "mc username '" + mc_username + "' is not a real Mojang username", false);
        }
        
        eb.setColor(failureColor);

        return eb.build();
    }
    
    public static MessageEmbed getUserAlreadyOnWhitelist(User messageAuthor, String mc_username) {
    	EmbedBuilder eb = new EmbedBuilder();
    	
    	eb.setTitle("Oops...");
    	eb.setDescription(mc_username + " is already on the whitelist!");
    	eb.setColor(infoColor);
    	
    	return eb.build();
    }
    
    public static MessageEmbed getUserNotOnWhitelist(User messageAuthor, String mc_username) {
    	EmbedBuilder eb = new EmbedBuilder();
    	
    	eb.setTitle("Oops...");
    	eb.setDescription(mc_username + " is not present on the whitelist!");
    	eb.setColor(infoColor);
    	
    	return eb.build();
    }
    
    public static MessageEmbed getUserBanned(User messageAuthor, String mc_username) {
    	EmbedBuilder eb = new EmbedBuilder();
    	
    	eb.setTitle(mc_username + " is banned!");
    	eb.setDescription("Unfortunately " + mc_username + " has been banned from the server and cannot be whitelisted. Contact an administrator if you feel this is an error.");
    	eb.setThumbnail("https://minotar.net/armor/bust/" + Utils.minecraftUsernameToUUID(mc_username) + "/100.png");
    	eb.setColor(failureColor);
    	
    	return eb.build();
    }
    
    public static MessageEmbed getInsufficientPerms(User messageAuthor, String commandTried) {
        EmbedBuilder eb = new EmbedBuilder();
        
        eb.setTitle("COMMAND DENIED");
        eb.addField("You do not have permission to use /" + commandTried, "contact @oddlyMetered if you think this is a mistake", false);
        
        eb.setColor(failureColor);

        return eb.build();
    }
    
    public static MessageEmbed getCommandOnly() {
    	EmbedBuilder eb = new EmbedBuilder();
    	eb.addField("Commands Only", "only commands can be written in this channel. Please use #general to chat and #issues to report a problem", false);
    	
    	eb.setColor(infoColor);
    	
    	return eb.build();
    }
    
    
    
    
    public static MessageEmbed makeSimpleInfoMessage(String message) {
    	EmbedBuilder eb = new EmbedBuilder();
    	eb.addField("Oops...", message, false);
    	eb.setColor(infoColor);
    	return eb.build();
    }
    
    public static MessageEmbed makeErrorMessage() {
    	EmbedBuilder eb = new EmbedBuilder();
    	eb.addField("AN ERROR OCCURED", "Please contact an administrator!", false);
    	eb.setColor(failureColor);
    	return eb.build();
    }
    
}
