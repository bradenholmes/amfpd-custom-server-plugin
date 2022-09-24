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
	private MySqlClient() {
		
	}
	
	
	public static Person queryPerson(String sql_query) throws SQLException {
		try (Connection con = Datasource.getConnection()){
			PreparedStatement statement = con.prepareStatement(sql_query);
			ResultSet res = statement.executeQuery();
			if (res.next()) {
				Person person = new Person();
				person.setPrimaryId(res.getInt("id"));
				person.setMinecraftUUID(res.getString("minecraft_id"));
				person.setMinecraftName(res.getString("minecraft_name"));
				person.setDiscordId(res.getLong("discord_id"));
				person.setDiscordName(res.getString("discord_name"));
				person.setWhitelisted(res.getBoolean("whitelisted"));
				person.setBanned(res.getBoolean("banned"));
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
				db.setMinecraftUUID(res.getString("mc_id"));
				db.setTime(res.getTimestamp("time"));
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
			columns.append("minecraft_id");
			values.append("'" + minecraftId + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(minecraftName)) {
			if (needComma) {
				columns.append(", ");
				values.append(", ");
			}
			columns.append("minecraft_name");
			values.append("'" + minecraftName.toUpperCase() + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(discordId)) {
			if (needComma) {
				columns.append(", ");
				values.append(", ");
			}
			columns.append("discord_id");
			values.append("'" + discordId + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(discordName)) {
			if (needComma) {
				columns.append(", ");
				values.append(", ");
			}
			columns.append("discord_name");
			values.append("'" + discordName + "'");
		}
		
		if (needComma) {
			columns.append(", ");
			values.append(", ");
		}
		columns.append("whitelisted");
		values.append(whitelisted);
		
		columns.append(", ");
		values.append(", ");
		columns.append("banned");
		values.append(banned);
		
		queryBuilder.append("INSERT INTO People (" + columns.toString() + ") VALUES (" + values.toString() + ")");
		
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

		queryBuilder.append("UPDATE People SET ");
		
		boolean needComma = false;
		if (!StringUtils.isEmpty(person.getMinecraftUUID())) {
			queryBuilder.append("minecraft_id='" + person.getMinecraftUUID() + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(person.getMinecraftName())) {
			if (needComma) {
				queryBuilder.append(", ");
			}
			queryBuilder.append("minecraft_name='" + person.getMinecraftName().toUpperCase() + "'");
			needComma = true;
		}
		if (person.getDiscordId() != 0) {
			if (needComma) {
				queryBuilder.append(", ");
			}
			queryBuilder.append("discord_id='" + person.getDiscordId() + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(person.getDiscordName())) {
			if (needComma) {
				queryBuilder.append(", ");
			}
			queryBuilder.append("discord_name='" + person.getDiscordName() + "'");
			needComma = true;
		}
		
		if (needComma) {
			queryBuilder.append(", ");
		}
		queryBuilder.append("whitelisted=" + person.isWhitelisted());
		
		queryBuilder.append(", ");
		queryBuilder.append("banned=" + person.isBanned());
		
		queryBuilder.append(" WHERE id=" + person.getPrimaryId());
		
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
			return queryPerson("SELECT id, minecraft_id, minecraft_name, discord_id, discord_name, whitelisted, banned FROM People WHERE id=" + primaryId);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Person searchPerson(String minecraftId, String minecraftName, String discordId, String discordName) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT id, minecraft_id, minecraft_name, discord_id, discord_name, whitelisted, banned FROM People WHERE ");
		boolean needAnd = false;
		if (!StringUtils.isEmpty(minecraftId)) {
			queryBuilder.append("minecraft_id='" + minecraftId + "'");
			needAnd = true;
		}
		if (!StringUtils.isEmpty(minecraftName)) {
			if (needAnd) {
				queryBuilder.append(" AND ");
			}
			queryBuilder.append("minecraft_name='" + minecraftName.toUpperCase() + "'");
			needAnd = true;
		}
		if (!StringUtils.isEmpty(discordId)) {
			if (needAnd) {
				queryBuilder.append(" AND ");
			}
			queryBuilder.append("discord_id='" + discordId + "'");
			needAnd = true;
		}
		if (!StringUtils.isEmpty(discordName)) {
			if (needAnd) {
				queryBuilder.append(" AND ");
			}
			queryBuilder.append("discord_name='" + discordName + "'");
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
		String query = "UPDATE People SET deaths = deaths + 1 WHERE minecraft_id='" + mc_uuid + "'";
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

		queryBuilder.append("INSERT INTO WhitelistEvents (caller_id, eventType, subject_id) VALUES ('" + event.callerId + "', '" + event.eventType + "', '" + event.subjectId + "')");
		
		try {
			execute(queryBuilder.toString());
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while inserting whitelist event");
			e.printStackTrace();
		}
	}
	
	public static void insertDeathBan(String mcUUID) {
		String query = "INSERT INTO deathban (mc_id, time) VALUES ('" + mcUUID + "', '" + new Timestamp(System.currentTimeMillis()) +"')";
		try {
			execute(query);
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while inserting a death for '" + mcUUID + "' ban");
			e.printStackTrace();
		}
	}
	
	public static DeathBan getDeathBan(String mcUUID) {
		try {
			return queryDeathBan("SELECT mc_id, time FROM deathban WHERE mc_id='" + mcUUID + "'");
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while getting a death ban for '" + mcUUID + "'");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void clearDeathBan(String mcUUID) {
		try {
			execute("DELETE FROM deathban WHERE mc_id='" + mcUUID + "'");
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("An SQL Exception occured while clearing a death ban for '" + mcUUID + "'");
			e.printStackTrace();
		}
	}
}
