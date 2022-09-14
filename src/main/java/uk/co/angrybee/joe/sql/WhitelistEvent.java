package uk.co.angrybee.joe.sql;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhitelistEvent
{
	int id;
	int callerId;
	String eventType;
	int subjectId;
}
