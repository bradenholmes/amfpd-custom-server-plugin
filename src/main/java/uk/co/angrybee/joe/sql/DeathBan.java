package uk.co.angrybee.joe.sql;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeathBan
{
	private String minecraftUUID;
	private Timestamp time;
	
	public boolean equals(Object b) {
		if (!(b instanceof DeathBan)) {
			return false;
		}
		DeathBan pb = (DeathBan)b;
		if (pb.minecraftUUID.equals(minecraftUUID)) {
			return true;
		}
		return false;
	}
}
