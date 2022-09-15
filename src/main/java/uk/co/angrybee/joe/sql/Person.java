package uk.co.angrybee.joe.sql;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Person
{
	private int primaryId;
	private String minecraftName;
	private long discordId = 0;
	private String discordName;
	private boolean whitelisted;
	private boolean banned;
	
	public boolean equals(Object b) {
		if (!(b instanceof Person)) {
			return false;
		}
		Person pb = (Person)b;
		if (pb.discordId != 0 && pb.discordId == this.discordId) {
			return true;
		}
		return false;
	}
}
