package uk.co.angrybee.joe.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.codehaus.plexus.util.StringUtils;

import uk.co.angrybee.joe.DiscordWhitelister;
import uk.co.angrybee.joe.Utils.WhitelistEventType;

public class MySqlClient
{
	
	private static final String PEOPLE_TABLENAME = "dwPeople";
	private static final String PEOPLE_ID = "id";
	private static final String PEOPLE_MCID = "minecraft_id";
	private static final String PEOPLE_MCNAME = "minecraft_name";
	private static final String PEOPLE_DCID = "discord_id";
	private static final String PEOPLE_DCNAME = "discord_name";
	private static final String PEOPLE_DEATHS = "deaths";
	private static final String PEOPLE_WHITELISTED = "whitelisted";
	private static final String PEOPLE_BANNED = "banned";
	
	private static final String WLEVENT_TABLENAME = "dwWhitelistEvents";
	private static final String WLEVENT_ID = "id";
	private static final String WLEVENT_CALLID = "caller_id";
	private static final String WLEVENT_EVENT = "event_type";
	private static final String WLEVENT_SUBJID = "subject_id";
	
	private static final String DEATHBAN_TABLENAME = "dwDeathBan";
	private static final String DEATHBAN_MCID = "mc_id";
	private static final String DEATHBAN_TIME = "time";
	
	
	
	private MySqlClient() {
		
	}
	
	public static void initializeDatabase() throws SQLException{
		String peopleTableCreation = "CREATE TABLE if not exists `" + PEOPLE_TABLENAME +"` ( "
				+ "`" + PEOPLE_ID + "` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
				+ "`" + PEOPLE_MCID + "` VARCHAR(40) NULL , "
				+ "`" + PEOPLE_MCNAME + "` VARCHAR(16) NULL , "
				+ "`" + PEOPLE_DCID + "` BIGINT(20) NOT NULL DEFAULT '0' , "
				+ "`" + PEOPLE_DCNAME + "` VARCHAR(32) NULL , "
				+ "`" + PEOPLE_DEATHS + "` INT NOT NULL DEFAULT '0' , "
				+ "`" + PEOPLE_WHITELISTED + "` BOOLEAN NOT NULL DEFAULT FALSE , "
				+ "`" + PEOPLE_BANNED + "` BOOLEAN NOT NULL DEFAULT FALSE)";
		
		String wlEventTableCreation = "CREATE TABLE if not exists `" + WLEVENT_TABLENAME +"` ( "
				+ "`" + WLEVENT_ID + "` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
				+ "`" + WLEVENT_CALLID + "` INT NOT NULL , "
				+ "`" + WLEVENT_EVENT + "` VARCHAR(8) NOT NULL , "
				+ "`" + WLEVENT_SUBJID + "` INT NOT NULL)";
		
		String deathBanTableCreation = "CREATE TABLE if not exists `" + DEATHBAN_TABLENAME +"` ( "
				+ "`" + DEATHBAN_MCID + "` VARCHAR(40) NOT NULL PRIMARY KEY, "
				+ "`" + DEATHBAN_TIME + "` TIMESTAMP NOT NULL)";
	

		try (Connection con = Datasource.getConnection()){
			PreparedStatement peopleStatement = con.prepareStatement(peopleTableCreation);
			peopleStatement.execute(peopleTableCreation);
			PreparedStatement wlEventStatement = con.prepareStatement(wlEventTableCreation);
			wlEventStatement.execute(wlEventTableCreation);
			PreparedStatement deathBanStatement = con.prepareStatement(deathBanTableCreation);
			deathBanStatement.execute(deathBanTableCreation);
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An Exception occured during initialization of database tables!");
			throw e;
		}
	}
	
	
	public static Person queryPerson(String sql_query) throws SQLException {
		try (Connection con = Datasource.getConnection()){
			PreparedStatement statement = con.prepareStatement(sql_query);
			ResultSet res = statement.executeQuery();
			if (res.next()) {
				Person person = new Person();
				person.setPrimaryId(res.getInt(PEOPLE_ID));
				person.setMinecraftUUID(res.getString(PEOPLE_MCID));
				person.setMinecraftName(res.getString(PEOPLE_MCNAME));
				person.setDiscordId(res.getLong(PEOPLE_DCID));
				person.setDiscordName(res.getString(PEOPLE_DCNAME));
				person.setWhitelisted(res.getBoolean(PEOPLE_WHITELISTED));
				person.setBanned(res.getBoolean(PEOPLE_BANNED));
				return person;
			} else {
				return null;
			}
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An Exception occured during person query '" + sql_query + "'!");
			throw e;
		}
	}
	
	public static DeathBan queryDeathBan(String sql_query) throws SQLException {
		try (Connection con = Datasource.getConnection()){
			PreparedStatement statement = con.prepareStatement(sql_query);
			ResultSet res = statement.executeQuery();
			if (res.next()) {
				DeathBan db = new DeathBan();
				db.setMinecraftUUID(res.getString(DEATHBAN_MCID));
				db.setTime(res.getTimestamp(DEATHBAN_TIME));
				return db;
			} else {
				return null;
			}
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An Exception occured during deathban query '" + sql_query + "'!");
			throw e;
		}
	}
	
	public static boolean execute(String sql_query) throws SQLException {
		try (Connection con = Datasource.getConnection()){
			PreparedStatement statement = con.prepareStatement(sql_query);
			if (statement.execute(sql_query)) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An Exception occured during execution of '" + sql_query + "'!");
			throw e;
		}
	}
	
	public static Person insertPerson(String minecraftId, String minecraftName, String discordId, String discordName, boolean whitelisted, boolean banned) {

		StringBuilder queryBuilder = new StringBuilder();

		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();
		
		boolean needComma = false;
		if (!StringUtils.isEmpty(minecraftId)) {
			columns.append(PEOPLE_MCID);
			values.append("'" + minecraftId + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(minecraftName)) {
			if (needComma) {
				columns.append(", ");
				values.append(", ");
			}
			columns.append(PEOPLE_MCNAME);
			values.append("'" + minecraftName.toUpperCase() + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(discordId)) {
			if (needComma) {
				columns.append(", ");
				values.append(", ");
			}
			columns.append(PEOPLE_DCID);
			values.append("'" + discordId + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(discordName)) {
			if (needComma) {
				columns.append(", ");
				values.append(", ");
			}
			columns.append(PEOPLE_DCNAME);
			values.append("'" + discordName + "'");
		}
		
		if (needComma) {
			columns.append(", ");
			values.append(", ");
		}
		columns.append(PEOPLE_WHITELISTED);
		values.append(whitelisted);
		
		columns.append(", ");
		values.append(", ");
		columns.append(PEOPLE_BANNED);
		values.append(banned);
		
		queryBuilder.append("INSERT INTO " + PEOPLE_TABLENAME + " (" + columns.toString() + ") VALUES (" + values.toString() + ")");
		
		try {
			execute(queryBuilder.toString());
			return searchPerson(minecraftId, minecraftName, discordId, discordName);
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while person insertion");
			e.printStackTrace();
			return null;
		}
	}
	
	public static Person updatePerson(Person person) {
		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append("UPDATE " + PEOPLE_TABLENAME +" SET ");
		
		boolean needComma = false;
		if (!StringUtils.isEmpty(person.getMinecraftUUID())) {
			queryBuilder.append(PEOPLE_MCID + "='" + person.getMinecraftUUID() + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(person.getMinecraftName())) {
			if (needComma) {
				queryBuilder.append(", ");
			}
			queryBuilder.append(PEOPLE_MCNAME + "='" + person.getMinecraftName().toUpperCase() + "'");
			needComma = true;
		}
		if (person.getDiscordId() != 0) {
			if (needComma) {
				queryBuilder.append(", ");
			}
			queryBuilder.append(PEOPLE_DCID + "='" + person.getDiscordId() + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(person.getDiscordName())) {
			if (needComma) {
				queryBuilder.append(", ");
			}
			queryBuilder.append(PEOPLE_DCNAME + "='" + person.getDiscordName() + "'");
			needComma = true;
		}
		
		if (needComma) {
			queryBuilder.append(", ");
		}
		queryBuilder.append(PEOPLE_WHITELISTED + "=" + person.isWhitelisted());
		
		queryBuilder.append(", ");
		queryBuilder.append(PEOPLE_BANNED + "=" + person.isBanned());
		
		queryBuilder.append(" WHERE " + PEOPLE_ID + "=" + person.getPrimaryId());
		
		try {
			execute(queryBuilder.toString());
			return getPerson(person.getPrimaryId());
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while person update");
			e.printStackTrace();
			return null;
		}
	}
	
	public static Person getPerson(int primaryId) {
		try {
			return queryPerson("SELECT " + PEOPLE_ID + ", " + PEOPLE_MCID + ", " + PEOPLE_MCNAME + ", " + PEOPLE_DCID + ", " + PEOPLE_DCNAME + ", " + PEOPLE_WHITELISTED + ", " + PEOPLE_BANNED + " FROM " + PEOPLE_TABLENAME + " WHERE " + PEOPLE_ID + "=" + primaryId);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Person searchPerson(String minecraftId, String minecraftName, String discordId, String discordName) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT " + PEOPLE_ID + ", " + PEOPLE_MCID + ", " + PEOPLE_MCNAME + ", " + PEOPLE_DCID + ", " + PEOPLE_DCNAME + ", " + PEOPLE_WHITELISTED + ", " + PEOPLE_BANNED + " FROM " + PEOPLE_TABLENAME + " WHERE ");
		boolean needAnd = false;
		if (!StringUtils.isEmpty(minecraftId)) {
			queryBuilder.append(PEOPLE_MCID + "='" + minecraftId + "'");
			needAnd = true;
		}
		if (!StringUtils.isEmpty(minecraftName)) {
			if (needAnd) {
				queryBuilder.append(" AND ");
			}
			queryBuilder.append(PEOPLE_MCNAME + "='" + minecraftName.toUpperCase() + "'");
			needAnd = true;
		}
		if (!StringUtils.isEmpty(discordId)) {
			if (needAnd) {
				queryBuilder.append(" AND ");
			}
			queryBuilder.append(PEOPLE_DCID + "='" + discordId + "'");
			needAnd = true;
		}
		if (!StringUtils.isEmpty(discordName)) {
			if (needAnd) {
				queryBuilder.append(" AND ");
			}
			queryBuilder.append(PEOPLE_DCNAME + "'" + discordName + "'");
		}
		
		try {
			return queryPerson(queryBuilder.toString());
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().warning("An SQL Exception occurred during person search:");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void logPlayerDeath(String mc_uuid) {
		String query = "UPDATE " + PEOPLE_TABLENAME + " SET " + PEOPLE_DEATHS + " = " + PEOPLE_DEATHS + " + 1 WHERE " + PEOPLE_MCID + "='" + mc_uuid + "'";
		try {
			execute(query);
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while logging a death for player '" + mc_uuid + "'");
			e.printStackTrace();
		}
	}
	
	public static void logWhitelistEvent(int callerPrimaryId, WhitelistEventType type, int subjectPrimaryId) {
		
		WhitelistEvent event = new WhitelistEvent();
		event.callerId = callerPrimaryId;
		event.eventType = type.toString();
		event.subjectId = subjectPrimaryId;
		
		insertWhitelistEvent(event);
	}
	
	private static void insertWhitelistEvent(WhitelistEvent event) {
		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append("INSERT INTO " + WLEVENT_TABLENAME + " (" + WLEVENT_CALLID + ", " + WLEVENT_EVENT + ", " + WLEVENT_SUBJID + ") VALUES ('" + event.callerId + "', '" + event.eventType + "', '" + event.subjectId + "')");
		
		try {
			execute(queryBuilder.toString());
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while inserting whitelist event");
			e.printStackTrace();
		}
	}
	
	public static void insertDeathBan(String mcUUID) {
		String query = "INSERT INTO " + DEATHBAN_TABLENAME + " (" + DEATHBAN_MCID + ", " + DEATHBAN_TIME+ ") VALUES ('" + mcUUID + "', '" + new Timestamp(System.currentTimeMillis()) +"')";
		try {
			execute(query);
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while inserting a death for '" + mcUUID + "' ban");
			e.printStackTrace();
		}
	}
	
	public static DeathBan getDeathBan(String mcUUID) {
		try {
			return queryDeathBan("SELECT * FROM " + DEATHBAN_TABLENAME + " WHERE " + DEATHBAN_MCID + "='" + mcUUID + "'");
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while getting a death ban for '" + mcUUID + "'");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void clearDeathBan(String mcUUID) {
		try {
			execute("DELETE FROM " + DEATHBAN_TABLENAME + " WHERE " + DEATHBAN_MCID + "='" + mcUUID + "'");
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while clearing a death ban for '" + mcUUID + "'");
			e.printStackTrace();
		}
	}
}
