//Ole Henrik Finsrud
//Innlevering 2
//PG3100, høst 2013

package model;

import java.sql.*;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class ConnectToDB implements AutoCloseable {
	
	private final String USER = "adminsmiz6zm";
	private final String PASSWORD = "Ry3kWXUl9kTA";
	private Connection connection;
	

	public ConnectToDB() {

	}
	
	public Connection getConnection() {
		try {
			MysqlDataSource ds = new MysqlDataSource();
			String serverName = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
			ds.setServerName(serverName);
			int port = Integer.parseInt(System.getenv("OPENSHIFT_MYSQL_DB_PORT"));
			ds.setPort(port);
			ds.setDatabaseName("nattogdagprosjekt");
			ds.setUser(USER);
			ds.setPassword(PASSWORD);
			connection = ds.getConnection();
			
//			MysqlDataSource ds = new MysqlDataSource();
//			ds.setServerName("localhost");
//			ds.setPort(3306);
//			ds.setDatabaseName("nattogdagprosjekt");
//			ds.setUser("root");
//			ds.setPassword("");
//			connection = ds.getConnection();
			
		} catch(SQLException e) {
			System.out.println("Connection error " + e);
		}
		return connection;			
				
	}
	
	public void close() {
		try {
			connection.close();
			
		} catch(SQLException e) {
			System.out.println("Error closing connection " + e);
		}		
	}
}
