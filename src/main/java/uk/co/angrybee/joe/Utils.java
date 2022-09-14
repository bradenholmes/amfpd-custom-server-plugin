package uk.co.angrybee.joe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class Utils {
	
    private static final char[] validCharacters = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h',
            'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '_'};
    
    public enum WhitelistEventType {ADD, REMOVE};
	
	public enum UsernameValidation {SUCCESS, LENGTH_FAIL, CHAR_FAIL, DNE_FAIL};
    public static UsernameValidation checkMcUsername(String nameToCheck) {
        // Length check
        if (nameToCheck.length() < 3 || nameToCheck.length() > 16) {
            return UsernameValidation.LENGTH_FAIL;
        }
        // Invalid char check
        for (char c : nameToCheck.toLowerCase().toCharArray()) {
            if (new String(validCharacters).indexOf(c) == -1) {
                return UsernameValidation.CHAR_FAIL;
            }
        }
        
        String uuid = minecraftUsernameToUUID(nameToCheck);
        if (uuid == null) {
        	DiscordWhitelister.getPluginLogger().info("mc username '" + nameToCheck + "' could not be found in mojang api. Failing validation...");
        	return UsernameValidation.DNE_FAIL;
        }
        
        return UsernameValidation.SUCCESS;
    }
    
    
    public static String minecraftUsernameToUUID(String minecraftUsername) {
        URL playerURL;
        String inputStream;
        BufferedReader bufferedReader;

        String playerUUID = null;

        try {
            playerURL = new URL("https://api.mojang.com/users/profiles/minecraft/" + minecraftUsername);
            bufferedReader = new BufferedReader(new InputStreamReader(playerURL.openStream()));
            inputStream = bufferedReader.readLine();

            if (inputStream != null) {
                JSONObject inputStreamObject = (JSONObject) JSONValue.parseWithException(inputStream);
                playerUUID = inputStreamObject.get("id").toString();
            }
        } catch (IOException | ParseException e) {
            return null;
        }

        return playerUUID;
    }
}

