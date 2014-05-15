package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.gson.Gson;


public class DBHandler {

//	public static void main(String[] args) throws SQLException {
//		
//		Marker[] markerArray = getmarkerArrayFromDB();
//		
//		for(Marker marker: markerArray) {
//			System.out.println(marker.toString());
//			System.out.println();
//		}
//		
////		System.out.println(DbHandler.getUserAutorization("petter85", "nattogdag"));
////		String user = "frode88";
////		String password = "NithGruppe5";
////		String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
////		
////		updateDB(user, hashed);
//
//	}
//	
	private static void insertUserToDB(String user, String passwordhash) {
		
		try (ConnectToDB cdb = new ConnectToDB();
			Connection connection = cdb.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO bruker "
					+ "(brukernavn, passord, navn, telefonnummer) VALUES (?, ?, ?, ?)")) {

			preparedStatement.setString(1, user);
			preparedStatement.setString(2, passwordhash);
			preparedStatement.setString(3, "Frode Nilsen");
			preparedStatement.setString(4, "78068678");
			
			System.out.println(preparedStatement.executeUpdate());
			
		
		} catch(SQLException ex) {
			System.out.println("Error" + ex);
		}
	
	}
	
	public static String insertReportToDB(String point_id, String delivered, String returns, 
			String user) {
		
		try (ConnectToDB cdb = new ConnectToDB();
			Connection connection = cdb.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "
					+ "distribusjon (distribusjonspunkt_id, antall_pafyllt, retur, "
					+ "bruker_id) VALUES (?, ?, ?, (SELECT bruker_id FROM bruker "
					+ "WHERE brukernavn = ?))")) {

			preparedStatement.setString(1, point_id);
			preparedStatement.setString(2, delivered);
			preparedStatement.setString(3, returns);
			preparedStatement.setString(4, user);
			
			return Integer.toString(preparedStatement.executeUpdate());
			
		
		} catch(SQLException ex) {
			
			return ex.getMessage();
		}
	
	}
		

		
	public static String getMarkerJsonFromDB() {
		String sqlQuery = "SELECT distribusjonspunkt_id, latitude, longitude, navn, adresse, bynavn "
		+ "FROM distribusjonspunkt WHERE bynavn='Oslo'";
		
		return queryDB(sqlQuery);
	}
	
	public static String getRouteFromDB(String user) {
		String sqlQuery = "select distribusjonspunkt.* from bruker "
				+ "left join bruker_avisrute on bruker.bruker_id= bruker_avisrute.bruker_id "
				+ "left join avisrute on bruker_avisrute.avisrutenummer=avisrute.avisrutenummer "
				+ "left join avisrutestopp on avisrute.avisrutenummer=avisrutestopp.avisrutenummer "
				+ "left join distribusjonspunkt on avisrutestopp.distribusjonspunkt_id="
				+ "distribusjonspunkt.distribusjonspunkt_id where brukernavn=" + "'" + user + "'";
		
		return queryDB(sqlQuery);

	}
	
	public static String queryDB(String sqlQuery) {
		String jsonString = "";
		try (ConnectToDB cdb = new ConnectToDB();
				Connection connection = cdb.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			
			ArrayList<MyMarker> markerList = new ArrayList<MyMarker>();
					
			while(resultSet.next()) {
				MyMarker marker = new MyMarker();
				marker.setId(resultSet.getInt("distribusjonspunkt_id"));
				marker.setLatitude(resultSet.getDouble("latitude"));
				marker.setLongitude(resultSet.getDouble("longitude"));
				marker.setName(resultSet.getString("navn"));
				marker.setAddress(resultSet.getString("adresse"));
				marker.setCity(resultSet.getString("bynavn"));
				markerList.add(marker);	
			}
			
			MyMarker[] markerArray = (MyMarker[]) markerList.toArray(new MyMarker[markerList.size()]);
		    Gson gson = new Gson();
		    jsonString = gson.toJson(markerArray);
			
		
			// Get an array of Marker objects.
//				Marker[] markerArray = gson.fromJson(jsonString, Marker[].class);
			
			
//				return markerArray;
			
		} catch(SQLException ex) {
			System.out.println("Error: " + ex);
		}
		
		return jsonString;
	}

}
