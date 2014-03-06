package no.nith.nattogdag;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;

public class MapActivity extends Activity implements OnMarkerClickListener,
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener, Serializable {
	
	private SharedPreferences prefs;
	private SharedPreferences savedValues;
	private String maptype;
	private LocationClient locationClient;
	private LocationRequest locationRequest;
	
	private GoogleMap map;
	private MyMarker myMarker;
	private String user;
	private String password;
	private static MyMarker[] markerArray;
	private Boolean firstclick;
	private static final int FASTEST_UPDATE_INTERVAL = 1000; // 1 second (locationlistener interface)
	Polyline polyline;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		locationClient = new LocationClient(this, this, this);
		
		savedValues = getSharedPreferences("savedValues", MODE_PRIVATE);
		
		
		Intent intent = getIntent();
		user = intent.getStringExtra("user");
		password = intent.getStringExtra("password");
		
		if(savedInstanceState != null) {
			user = savedInstanceState.getString("user");
			password = savedInstanceState.getString("password");
		}
		
		firstclick = false;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("user", user);
		outState.putString("password", password);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onStart() {
		if(prefs.getBoolean("pref_enable_gps", true) & prefs.getBoolean("pref_enable_map_tracking", true)) {
			setupLocation();
		}
		super.onStart();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(prefs.getBoolean("pref_enable_gps", true) & !locationClient.isConnected()) {
			locationClient.connect();
		}
	
		maptype = prefs.getString("pref_map", "MAP_TYPE_NORMAL");
		setUpMapIfNeeded();
	}
	
	
	@Override
	protected void onPause() {
		saveZoomAndLocation();
		map = null;
		
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		if(locationClient.isConnected()) {
			locationClient.disconnect();
		}
		super.onStop();
	}
	
	private void setupLocation() {
		int update_interval = Integer.parseInt(prefs.getString("pref_location_update", "2")) * 1000;
		locationRequest = LocationRequest.create()
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setInterval(update_interval)
				.setFastestInterval(FASTEST_UPDATE_INTERVAL);
	}
	
	// Saves current map location and zoom level
	public void saveZoomAndLocation() {
		// Get location and zoom.
		float zoom = map.getCameraPosition().zoom;
		LatLng currentPosition = map.getCameraPosition().target;
		float currentLatitude = (float)currentPosition.latitude;
		float currentLongitude = (float)currentPosition.longitude;
		// Save values to SharedPreferences.
		Editor editor = savedValues.edit();
		editor.putFloat("zoom", zoom);
		editor.putFloat("currentLatitude", currentLatitude);
		editor.putFloat("currentLongitude", currentLongitude);
		editor.commit();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_settings:
				startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
				return true;
				
			case R.id.menu_getDirections:
				if (polyline != null) {
					polyline.remove();
					polyline = null;
				}
				if (prefs.getBoolean("pref_enable_directions", true)) {
					firstclick = true;
					String toast = "Velg startpunkt på ruten";
					Toast.makeText(MapActivity.this, toast, Toast.LENGTH_SHORT).show();	
				} else {
					String toast = "Veibeskrivelse er deaktivert i innstillinger";
					Toast.makeText(MapActivity.this, toast, Toast.LENGTH_LONG).show();
				}
					
				return true;
				
			default: 
				return false;
		}
	}
	

	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (map == null) {
	        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
	                            .getMap();
	        // Check if we were successful in obtaining the map.
	        if (map != null) {
	            // The Map is verified. It is now safe to manipulate the map.
	        	setMapType();
	        	map.setOnMarkerClickListener(this);
	        	
	        	Boolean gpsPrefs = prefs.getBoolean("pref_enable_gps", true);
	        	map.setMyLocationEnabled(gpsPrefs);
	        	Boolean trackingPrefs = prefs.getBoolean("pref_enable_map_tracking", true);
	        	map.getUiSettings().setMyLocationButtonEnabled(!trackingPrefs);
	        	
	        	setZoomAndLocation();
	        	
	        	String getRouteURL = "https://nattogdagprosjekt-nith.rhcloud.com/NattogDag/" +
						"JsonServlet?user=" + user + "&password=" + password +
						"&command=getRoute";
	        	
	        	new GetMyMarkers().execute(getRouteURL);

	        }
	    }	    	    
	}
	
	public void setZoomAndLocation() {
		double currentLatitude = (double)savedValues.getFloat("currentLatitude", 59.90202436f);
		double currentLongitude = (double)savedValues.getFloat("currentLongitude", 10.75698853f);
		LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);
		float zoom = savedValues.getFloat("zoom", 10);
		
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom));
	}
	
	
	private void setMapType() {
		if(maptype.equals("MAP_TYPE_NORMAL")) {
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		} else if(maptype.equals("MAP_TYPE_HYBRID")) {
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		} else  if(maptype.equals("MAP_TYPE_HYBRID")) {
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		} else if(maptype.equals("MAP_TYPE_TERRAIN")) {
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		} else if(maptype.equals("MAP_TYPE_SATELLITE")) {
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		}
	}
	

	
	class GetMyMarkers extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String urlString = params[0];
			String jsonString = null;
			
			try {
				jsonString = Internet.getJsonString(urlString);
				
			} catch (Exception e) {
				Log.e("GetMarkers", e.toString());
			} 
			return jsonString;
		}
		
		@Override
		protected void onPostExecute(String jsonString) {
			if (jsonString != null) {
				Editor editor = savedValues.edit();
				editor.putString("jsonString", jsonString);
				editor.commit();
			}
			addMarkers();		
		}	
	}
	
	class GetDirectionsFromGoogle extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String directionsUrl = params[0];
			String jsonString = Internet.getJsonString(directionsUrl);
			Log.e("JsonString", jsonString);
			return jsonString;
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			if (result != null) {
				Directions directions = new Directions(result);
				Log.e("PolylineEncoded", directions.getPolylineEncoded());
				List<LatLng> polyLineList = directions.getPolylineDecoded();
				PolylineOptions rectLine = new PolylineOptions()
						.addAll(polyLineList);
				polyline = map.addPolyline(rectLine);
				double distance = directions.getDistance();
				String toast = "Lengde på ruten: " + distance + " Km."
						+ "\nEstimert tidsbruk: " + directions.duration;
				Toast.makeText(MapActivity.this, toast, Toast.LENGTH_LONG)
						.show();
				Toast.makeText(MapActivity.this, toast, Toast.LENGTH_LONG)
						.show();
			}
			
			super.onPostExecute(result);
		}
		
	}
	
	private void addMarkers() {
		Gson gson = new Gson(); // <-- Using Gson to convert the Json to an array of 
		// MyMarker objects
		String jsonString = savedValues.getString("jsonString", null);
		
		markerArray = gson.fromJson(jsonString, MyMarker[].class);
		for(MyMarker mymarker: markerArray) {
			String snippet = mymarker.getAddress() + " " + mymarker.getCity();
			map.addMarker(new MarkerOptions().position(mymarker.getLatlng())
				.title(mymarker.getName())
				.snippet(snippet));
			
			
		}
		
	}
	
	public void addPolyLine(Marker onClickMarker) {
//		Location location = locationClient.getLastLocation();
//		if (location != null) {
//			MyMarker closestMarker = findClosestMarker(new LatLng(location.getLatitude(), 
//					location.getLongitude()));
//			Toast.makeText(MapActivity.this, "Closest stop is "
//					+ closestMarker.getName() + " " + closestMarker.getAddress(), 
//					Toast.LENGTH_SHORT).show();
//		}
		MyMarker furtherestMarker = findFurtherestMarker(onClickMarker.getPosition());
		MyMarker onClickMyMarker = null;
		String tempSnippet = onClickMarker.getSnippet();
		Log.e("tempSnippet", tempSnippet);
		for(MyMarker myMarker: markerArray) {
			String tempSnippet2 = myMarker.getAddress();
			if(tempSnippet.contains(tempSnippet2)) {
				Log.e("tempSnippet2", tempSnippet2);
				Log.e("Yipee!!", "We have a match!!!!");
				onClickMyMarker = myMarker;
				Log.e("Yipee!!", onClickMyMarker.getAddress());
			}
		}
		
		createDirectionsURL(furtherestMarker, onClickMyMarker);
//		Log.e("Yipee!!", marker.getId());
//		Toast.makeText(MapActivity.this, temp.getId(), Toast.LENGTH_SHORT).show();
	}
	
	public void createDirectionsURL(MyMarker furtherestMarker, MyMarker onClickMyMarker) {

		String waypoints = "";
		for(MyMarker myMarker: markerArray) {
			if(!myMarker.equals(furtherestMarker) & !myMarker.equals(onClickMyMarker)) {
				waypoints += "|" + myMarker.getLatitude() + "," + myMarker.getLongitude();
			}
		}
		
		Log.e("Waypoints!", waypoints);
		
		String directionsUrl = "https://maps.googleapis.com/maps/api/directions/";
		
		directionsUrl += "json?origin=" + onClickMyMarker.getLatitude() + "," + onClickMyMarker.getLongitude();
		
		directionsUrl += "&destination=" + furtherestMarker.getLatitude() + "," + furtherestMarker.getLongitude();
		
		directionsUrl += "&waypoints=optimize:true" + waypoints;
		
		String gpsPrefs = Boolean.toString(prefs.getBoolean("pref_enable_gps", true));
		directionsUrl += "&sensor=" + gpsPrefs;
		
		directionsUrl += "&mode=" + prefs.getString("pref_movement", "driving");
		
		if(prefs.getBoolean("pref_avoid_highways", false)) {
			directionsUrl += "&avoid=highways";
			if(prefs.getBoolean("pref_avoid_tolls", false))
				directionsUrl += "|tolls";
		}
		
		if(prefs.getBoolean("pref_avoid_tolls", false)) {
			directionsUrl += "&avoid=tolls";
			if(prefs.getBoolean("pref_avoid_highways", false))
				directionsUrl += "|highways";
		}
		
		Log.e("DirectionsURL!!!!", directionsUrl);
		
		new GetDirectionsFromGoogle().execute(directionsUrl);

	}
	
	
	// Calculates distance between two coordinates using Geodesy.
	public double calculateDistance(LatLng pointA, LatLng pointB) {
		GeodeticCalculator geoCalc = new GeodeticCalculator();

		Ellipsoid reference = Ellipsoid.WGS84;  
		
		// Point A
		GlobalPosition positionA = new GlobalPosition(pointA.latitude, pointA.longitude, 0.0); 
		
		// Point B
		GlobalPosition positionB = new GlobalPosition(pointB.latitude, pointB.longitude, 0.0);
		
		// Distance between Point A and Point B
		double distance = geoCalc.calculateGeodeticCurve(reference, positionA, positionB).
				getEllipsoidalDistance();
		
		return distance;
	}
	
	// Find the closest marker in the route from the reference point. 
	public MyMarker findClosestMarker(LatLng reference) {
		double distance = 500000000;
		MyMarker closestMarker = null;
		for(MyMarker marker: markerArray) {	
			double thisDistance = calculateDistance(reference, marker.getLatlng());
			if(thisDistance < distance) {
				distance = thisDistance;
				closestMarker = marker;
			}
		}
		return closestMarker;
	}
	
	// Find the furtherest marker in the route from the reference point. 
	public MyMarker findFurtherestMarker(LatLng reference) {
		double distance = 0;
		MyMarker furtherestMarker = null;
		for(MyMarker marker: markerArray) {	
			double thisDistance = calculateDistance(reference, marker.getLatlng());
			if(thisDistance > distance) {
				distance = thisDistance;
				furtherestMarker = marker;
			}
		}
//		Toast.makeText(MapActivity.this, "Furtherest marker is " + furtherestMarker.getName()
//				+ " " + furtherestMarker.getAddress(), Toast.LENGTH_SHORT).show();
		return furtherestMarker;
	}
	
	
	// implement OnMarkerClickListener interface
	@Override
	public boolean onMarkerClick(Marker marker) {
		if(firstclick) {
			Marker tempMarmer = marker;
			addPolyLine(tempMarmer);
			firstclick = false;
		}
		
		return false;
	}
	
	// Implement ConnectionCallbacks interface
	@Override
	public void onConnected(Bundle bundle) {
		if(prefs.getBoolean("pref_enable_gps", true) & prefs.getBoolean("pref_enable_map_tracking", true)) {
			locationClient.requestLocationUpdates(locationRequest, this);
		}

	}
	
	// Implement ConnectionCallbacks interface
	@Override
	public void onDisconnected() {
		if (locationClient.isConnected()) {
			locationClient.removeLocationUpdates(this);
		}
		
	}
	
	// Implement LocationListener interface
	@Override
	public void onLocationChanged(Location location) {
		Toast.makeText(MapActivity.this, location.getLatitude() + " " + location.getLongitude(), 
				Toast.LENGTH_SHORT).show();
//				LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
//				float zoom = savedValues.getFloat("zoom", 10);
				map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), 
						location.getLongitude())));
	}
	
	
	// implement OnConnectionFailedListener interface
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		
	}
	

}
