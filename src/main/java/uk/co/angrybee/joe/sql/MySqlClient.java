package uk.co.angrybee.joe.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.codehaus.plexus.util.StringUtils;

import uk.co.angrybee.joe.DiscordWhitelister;
import uk.co.angrybee.joe.Utils.WhitelistEventType;

public class MySqlClient
{
	String url = "jdbc:mysql://mysql.apexhosting.gdn:3306/apexMC521038";
	String user = "apexMC521038";
	String pass = "k#QqYRV^bQdHnWoz@tkDxleE";
	
	
	public static MySqlClient instance = null;
	
	private boolean connectionOpen = false;
	private Connection connection;
	
	private MySqlClient() {
		
		try {
			
			connection = DriverManager.getConnection(url, user, pass);
			connectionOpen = true;
			DiscordWhitelister.getPluginLogger().log(Level.INFO, "CONNECTED!");

		} catch (SQLException e){
			e.printStackTrace();
			DiscordWhitelister.getPluginLogger().log(Level.INFO, "SQL exception!");
		}
	}
	
	public static MySqlClient get() {
		if (instance == null) {
			instance = new MySqlClient();
		}
		return instance;
	}
	
	
	public ResultSet query(String sql_query) throws SQLException{
		return connection.createStatement().executeQuery(sql_query);
	}
	
	public void insertPerson(String minecraftName, String discordId, String discordName, boolean whitelisted, boolean banned) {
		StringBuilder queryBuilder = new StringBuilder();

		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();
		
		boolean needComma = false;
		if (!StringUtils.isEmpty(minecraftName)) {
			columns.append("minecraft_name");
			values.append("'" + minecraftName + "'");
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
			connection.createStatement().execute(queryBuilder.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updatePerson(int primaryId, String minecraftName, String discordId, String discordName, boolean whitelisted, boolean banned) {
		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append("UPDATE People SET ");
		
		boolean needComma = false;
		if (!StringUtils.isEmpty(minecraftName)) {
			queryBuilder.append("minecraft_name='" + minecraftName + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(discordId)) {
			if (needComma) {
				queryBuilder.append(", ");
			}
			queryBuilder.append("discord_id='" + discordId + "'");
			needComma = true;
		}
		if (!StringUtils.isEmpty(discordName)) {
			if (needComma) {
				queryBuilder.append(", ");
			}
			queryBuilder.append("discord_name='" + discordName + "'");
			needComma = true;
		}
		
		if (needComma) {
			queryBuilder.append(", ");
		}
		queryBuilder.append("whitelisted=" + whitelisted);
		
		queryBuilder.append(", ");
		queryBuilder.append("banned=" + banned);
		
		queryBuilder.append(" WHERE id=" + primaryId);
		
		try {
			connection.createStatement().execute(queryBuilder.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Person getPerson(int primaryId) {
		try {
			ResultSet result = query("SELECT id, minecraft_name, discord_id, discord_name, whitelisted, banned FROM People WHERE id=" + primaryId);
			result.next();
			Person person = new Person();
			person.setPrimaryId(result.getInt("id"));
			person.setMinecraftName(result.getString("minecraft_name"));
			person.setDiscordId(result.getLong("discord_id"));
			person.setDiscordName(result.getString("discord_name"));
			person.setWhitelisted(result.getBoolean("whitelisted"));
			person.setBanned(result.getBoolean("banned"));
			return person;
		} catch (Exception e) {
			return null;
		}
	}
	
	public Person searchPerson(String minecraftName, String discordId, String discordName) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("SELECT id, minecraft_name, discord_id, discord_name, whitelisted, banned FROM People WHERE ");
		boolean needAnd = false;
		if (!StringUtils.isEmpty(minecraftName)) {
			queryBuilder.append("minecraft_name='" + minecraftName + "'");
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
			ResultSet results = query(queryBuilder.toString());
			if (results.next()) {
				Person person = new Person();
				person.setPrimaryId(results.getInt("id"));
				person.setMinecraftName(results.getString("minecraft_name"));
				person.setDiscordId(results.getLong("discord_id"));
				person.setDiscordName(results.getString("discord_name"));
				person.setWhitelisted(results.getBoolean("whitelisted"));
				person.setBanned(results.getBoolean("banned"));
				return person;
			} else {
				return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}
	
	public void logWhitelistEvent(String callerDiscordId, WhitelistEventType type, String subjectMcUser) {
		
		Person caller = searchPerson("", callerDiscordId, "");
		
		if (caller == null) {
			DiscordWhitelister.getPluginLogger().log(Level.SEVERE, "Unidentified user attempted to change the whitelist");
			return;
		}
		
		WhitelistEvent event = new WhitelistEvent();
		event.callerId = caller.getPrimaryId();
		event.eventType = type.toString();

		if (caller.getMinecraftName().equals(subjectMcUser)) {
			event.subjectId = caller.getPrimaryId();
		} else {
			Person subject = searchPerson(subjectMcUser, "", "");
			if (subject == null) {
				insertPerson(subjectMcUser, "", "", false, false);
			}
			
			subject = searchPerson(subjectMcUser, "", "");
			if (subject == null) {
				DiscordWhitelister.getPluginLogger().log(Level.SEVERE, "Failed to make a new Person object out of newly whitelisted mc_username");
				return;
			} else {
				if (type == WhitelistEventType.ADD) {
					updatePerson(subject.getPrimaryId(), "", "", "", true, subject.isBanned());
				} else if (type == WhitelistEventType.REMOVE) {
					updatePerson(subject.getPrimaryId(), "", "", "", false, subject.isBanned());
				}
			}
			
			
			event.setSubjectId(subject.getPrimaryId());
		}
		
		insertWhitelistEvent(event);
	}
	
	public void logWhitelistEventSimple(int callerPrimaryId, WhitelistEventType type, int subjectPrimaryId) {
		
		Person caller = getPerson(callerPrimaryId);
		Person subject = getPerson(subjectPrimaryId);
		
		if (caller == null) {
			DiscordWhitelister.getPluginLogger().log(Level.SEVERE, "Unidentified user attempted to change the whitelist");
			return;
		}
		
		if (subject == null) {
			return;
		}
		
		WhitelistEvent event = new WhitelistEvent();
		event.callerId = caller.getPrimaryId();
		event.eventType = type.toString();
		event.subjectId = subject.getPrimaryId();
		
		insertWhitelistEvent(event);
	}
	
	public void insertWhitelistEvent(WhitelistEvent event) {
		StringBuilder queryBuilder = new StringBuilder();

		
		queryBuilder.append("INSERT INTO WhitelistEvents (caller_id, eventType, subject_id) VALUES ('" + event.callerId + "', '" + event.eventType + "', '" + event.subjectId + "')");
		
		try {
			connection.createStatement().execute(queryBuilder.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnection() throws SQLException{
		if (connectionOpen) {
			connection.close();
			connectionOpen = false;
		}
	}
}
