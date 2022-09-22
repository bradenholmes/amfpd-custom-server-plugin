package uk.co.angrybee.joe.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;

import uk.co.angrybee.joe.DiscordWhitelister;

public class Datasource
{
	
	private static final String URL = "jdbc:mysql://mysql.apexhosting.gdn:3306/apexMC521038";
	private static final String USER = "apexMC521038";
	private static final String PASS = "k#QqYRV^bQdHnWoz@tkDxleE";
	
	private static Datasource instance;
	
	private HikariDataSource ds;
	
	private Datasource() {
		ds = new HikariDataSource();
		
		ds.setMaximumPoolSize(10);
		ds.setJdbcUrl(URL);
		ds.setUsername(USER);
		ds.setPassword(PASS);
		
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
