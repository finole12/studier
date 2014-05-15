package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AuthenticateUser {
	
	public static boolean getUserAuthentication(String user, String password) {
		//Get the passwordhash from the database.
		String passwordHash = getPasswordhashFromDB(user);
		
		// If the username does not exist in the database it will return an empty string as the hash,
		// and authentication should fail.
		if (passwordHash != "") {
			// Uses jBCrypt to check the plain text password against the hash, and returns
			// true if it matches, if not, false. 
			// (If the hash is an empty String it will throw an exception)
			return BCrypt.checkpw(password, passwordHash);
		} else return false;
	}
	
	private static String getPasswordhashFromDB(String user) {
		String passwordHash = "";
		
		try (ConnectToDB cdb = new ConnectToDB();
			Connection connection = cdb.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT passord FROM bruker" +
				" WHERE brukernavn = ?")) {
		
			preparedStatement.setString(1, user);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			passwordHash = resultSet.getString("passord");
			
			//closing resources
			resultSet.close();
		
		} catch(SQLException ex) {
			
		}
		
		return passwordHash;
		
		
	}
	

}
