package uk.co.angrybee.joe;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class AuthorPermissions
{
    private boolean userCanAdd = true;
    private boolean userCanRemove = false;
    

    public AuthorPermissions(SlashCommandEvent event) {
        for (Role role : event.getMember().getRoles())
        {
            if (Arrays.stream(DiscordClient.allowedToRemoveRoles).parallel().anyMatch(role.getId()::equalsIgnoreCase))
            {
                userCanRemove = true;
                break;
            }
        }

        for (Role role : event.getGuild().getMember(event.getUser()).getRoles())
        {
            if (Arrays.stream(DiscordClient.allowedToAddRoles).parallel().anyMatch(role.getId()::equalsIgnoreCase))
            {
                userCanAdd = true;
                break;
            }
        }

    }
    
    public AuthorPermissions(MessageReceivedEvent event)
    {
        for (Role role : event.getGuild().getMember(event.getAuthor()).getRoles())
        {
        	
        	if (Arrays.stream(DiscordClient.allowedToAddRoles).parallel().anyMatch(role.getId()::equalsIgnoreCase)) {
        		userCanAdd = true;
        		break;
        	}
        }


        for (Role role : event.getGuild().getMember(event.getAuthor()).getRoles())
        {
            if (Arrays.stream(DiscordClient.allowedToRemoveRoles).parallel().anyMatch(role.getId()::equalsIgnoreCase))
            {
                userCanRemove = true;
                break;
            }
        }

    }

    public boolean isUserCanAdd() {
        return userCanAdd;
    }
    
    public boolean isUserCanRemove() {
        return userCanRemove;
    }


   
}
