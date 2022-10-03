package uk.co.angrybee.joe.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;

import uk.co.angrybee.joe.DiscordWhitelister;

public class Datasource
{
	
	private static Datasource instance;
	
	private HikariDataSource ds;
	
	private Datasource() {
		ds = new HikariDataSource();
		
		ds.setMaximumPoolSize(10);
		
		StringBuilder url = new StringBuilder();
		url.append("jdbc:mysql://");
		url.append(DiscordWhitelister.mainConfig.getFileConfiguration().getString("mysql-database-host"));
		url.append(":");
		url.append(DiscordWhitelister.mainConfig.getFileConfiguration().getString("mysql-database-port"));
		url.append("/");
		url.append(DiscordWhitelister.mainConfig.getFileConfiguration().getString("mysql-database-name"));
		
		ds.setJdbcUrl(url.toString());
		ds.setUsername(DiscordWhitelister.mainConfig.getFileConfiguration().getString("mysql-database-user"));
		ds.setPassword(DiscordWhitelister.mainConfig.getFileConfiguration().getString("mysql-database-pass"));
		
		try {
			ds.getConnection();
			DiscordWhitelister.getPluginLogger().info("Connected to DB!");
		} catch (SQLException e) {
			DiscordWhitelister.getPluginLogger().severe("Failed to connect to DB!");
		}
	}
	
	public static void start() {
		if (instance != null) {
			return;
		}
		
		instance = new Datasource();
	}
	
	public static void close() {
		//instance.ds.close();
	}
	
	public static Connection getConnection() throws SQLException {
		if (instance == null) {
			return null;
		}
		return instance.ds.getConnection();
	}
	

}
