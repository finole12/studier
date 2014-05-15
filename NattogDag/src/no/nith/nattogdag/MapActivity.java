package no.nith.nattogdag;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MapActivity extends FragmentActivity implements OnMarkerClickListener,
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener, Serializable, 
		OnInfoWindowClickListener {
	
	private SharedPreferences prefs;
	private SharedPreferences savedValues;
	private String maptype;
	private LocationClient locationClient;
	private LocationRequest locationRequest;
	private ProgressDialog pd;
	
	private GoogleMap map;
	private Marker onClickMarker;
	private HashMap<String, Integer> markerIDMap;
	private HashMap<String, MyMarker> markerMap;
	MyMarker onClickMyMarker;
	private String user;
	private String password;
	private static MyMarker[] markerArray;
	private Boolean firstclick;
	private static final int FASTEST_UPDATE_INTERVAL = 1000; // 1 second (locationlistener interface)
	// Get route updates from server if it is more than 24 hours since last update.
	private static final long ROUTE_UPDATE_INTERVAL = 24;
	private static final String SERVER_URL = "https://nattogdagprosjekt-nith.rhcloud.com/NattogDag/" +
			"JsonServlet";
	Polyline polyline;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		setTitle("Rutekart");
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
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
			
		if(updateOnInterval()) {    	
        	new GetMyMarkers().execute(SERVER_URL, user, password);
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
		saveMarkerArray();
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
	
	// Kode hentet fra : http://stackoverflow.com/questions/2257963/
	// how-to-show-a-dialog-to-confirm-that-the-user-wishes-to-exit-an-android-activity.
	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
        .setMessage("Er du sikker på at du vil avslutte?")
        .setCancelable(false)
        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 MapActivity.this.finish();
            }
        })
        .setNegativeButton("Nei", null)
        .show();
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
				
			case R.id.menu_getMyMarkers:
	        	
	        	new GetMyMarkers().execute(SERVER_URL, user, password);
	        	return true;
	        	
			case R.id.menu_resetMarkers:
				ResetDialogFragment dialog = new ResetDialogFragment();
				dialog.show(getFragmentManager(), "Tilbakestill");
				return true;
				
			default: 
				return false;
		}
	}
	
	// Check how long since the last time a new route was downloaded from server, and returns
	// true if new route should be downloaded.
	public boolean updateOnInterval() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Oslo"));
		String dateTime = sdf.format(cal.getTime());
		// Get the last time the app checked for updates.
		String savedDateTime = savedValues.getString("dateTime", "1970-01-03 11:23:00");
		
		Editor editor = savedValues.edit();	// Saves current date and time to SharedPreferences.
		editor.putString("dateTime", dateTime);
		editor.commit();	
		long minutes;	//Difference between date and time in minutes
		long seconds;	//Difference in seconds
		long hours;		//Difference in hours
		
		try {
			Date currentDateTime = sdf.parse(dateTime);
			Date previousDateTime = sdf.parse(savedDateTime);

			long differenceInMilliseconds = currentDateTime.getTime() - previousDateTime.getTime();
			minutes = differenceInMilliseconds/1000/60;
			seconds = differenceInMilliseconds/1000;
			hours = minutes/60/24;
			
	    	// Return true if difference is more than 24 hours.
			if(hours > ROUTE_UPDATE_INTERVAL) {
				return true;
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
		
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
	        	map.setOnInfoWindowClickListener(this);
	        	
	        	Boolean gpsPrefs = prefs.getBoolean("pref_enable_gps", true);
	        	map.setMyLocationEnabled(gpsPrefs);
	        	Boolean trackingPrefs = prefs.getBoolean("pref_enable_map_tracking", true);
	        	map.getUiSettings().setMyLocationButtonEnabled(!trackingPrefs);
	        	
	        	setZoomAndLocation();
	        	
	        	addMarkers();

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
	

	// Download the route from server in a separate thread.
	class GetMyMarkers extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(MapActivity.this);
			pd.setTitle("Laster ned ruteoppdateringer...");
			pd.setMessage("Vennligst vent.");
			pd.setCancelable(true);
			pd.setIndeterminate(true);
			pd.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String host = params[0];
			String user = params[1];
			String password = params[2];
			
			String jsonString = null;
			
			try {
				jsonString = Internet.sendPostRequest(host, user, password, "getRoute");
				
			} catch (Exception e) {
				Log.e("GetMarkers", e.toString());
			} 
			return jsonString;
		}
		
		@Override
		protected void onPostExecute(String jsonString) {
			if (pd!=null) {
				pd.dismiss();
			}
			
			if (jsonString != null) {
				String updatedJsonString = setDelivered(jsonString);
				Editor editor = savedValues.edit();
				if (updatedJsonString != null) {	
					editor.putString("jsonString", updatedJsonString);
					editor.commit();
				} else {
					editor.putString("jsonString", jsonString);
					editor.commit();
				}
			}
			
			map = null;
			setUpMapIfNeeded();
		}	
	}
	
	class GetDirectionsFromGoogle extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String directionsUrl = params[0];
			String jsonString = Internet.getJsonString(directionsUrl);
			
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
	
	// Sets the state of delivery in the downloaded MyMarker objects to the state of the
	// MyMarker objects stored in SharedPreferences (So no information is lost when a new route
	// is downloaded from server).
	public String setDelivered(String downloadedJsonString) {
		Gson gson = new Gson(); // <-- Using Gson to convert the Json to an array of 
		// MyMarker objects
		MyMarker[] downloadedMyMarkerarray = null;
		
		try {
			String savedJsonString = savedValues.getString("jsonString", null);
			MyMarker[] savedMyMarkerarray;
			if(markerArray == null) {
				savedMyMarkerarray = gson.fromJson(savedJsonString, MyMarker[].class);
			} else {
				savedMyMarkerarray = markerArray;
			}
			downloadedMyMarkerarray = gson.fromJson(downloadedJsonString, MyMarker[].class);
			for(MyMarker savedMarker: savedMyMarkerarray) {
				for(MyMarker downloadedMarker: downloadedMyMarkerarray) {
					if(savedMarker.getId() == downloadedMarker.getId()) {
						boolean delivered = savedMarker.getHasBeenDelivered();
						if (delivered) {
							downloadedMarker.setHasBeenDelivered(delivered);
							downloadedMarker.setDateTime(savedMarker.getDateTime());
							downloadedMarker.setDelivered(savedMarker.getDelivered());
							downloadedMarker.setReturns(savedMarker.getReturns());
						}
					}
				}
				
			}
		} catch(NullPointerException ex) {
			return null;
		}
		
		return gson.toJson(downloadedMyMarkerarray);
	}
	
	// Add Markers to the map.
	private void addMarkers() {
		
		if(savedValues.getString("jsonString", null)  != null) {
			Gson gson = new Gson(); // <-- Using Gson to convert the Json to an array of 
			// MyMarker objects
			String jsonString = savedValues.getString("jsonString", null);
			markerIDMap = new HashMap<String, Integer>();
			markerMap = new HashMap<String, MyMarker>();
			
			markerArray = gson.fromJson(jsonString, MyMarker[].class);
			if(markerArray != null) {

				for(MyMarker mymarker: markerArray) {
					String snippet = mymarker.getAddress() + " "
							+ mymarker.getCity();
					String mapKey = mymarker.getName() + snippet;
					markerMap.put(mapKey, mymarker);
					if (!mymarker.getHasBeenDelivered()) {	
						markerIDMap.put(mapKey, mymarker.getId());
						map.addMarker(new MarkerOptions()
								.position(mymarker.getLatlng())
								.title(mymarker.getName())
								.snippet(snippet));
					} else {
						String snippet2 = "Levert: " + mymarker.getDelivered() + 
                     		" Retur: " + mymarker.getReturns();
						String mapKey2 = mymarker.getDateTime() + snippet2;
						markerIDMap.put(mapKey2, mymarker.getId());
						markerMap.put(mapKey2, mymarker);
						map.addMarker(new MarkerOptions()
	                         .position(mymarker.getLatlng())
	                         .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
	                         .title(mymarker.getDateTime())
	                         .snippet(snippet2));
						
					}
				}
			} else {
				String toast = "Noe gikk galt med nedlastingen. \nVennligst prøv igjen.";
				Toast.makeText(MapActivity.this, toast, Toast.LENGTH_LONG)
					.show();
				Toast.makeText(MapActivity.this, toast, Toast.LENGTH_LONG)
					.show();
				Toast.makeText(MapActivity.this, toast, Toast.LENGTH_LONG)
					.show();
			}
			
		} 
		
	}
	
	public void addPolyLine(Marker onClickMarker) {

		MyMarker furtherestMarker = findFurtherestMarker(onClickMarker.getPosition());
		String mapKey = onClickMarker.getTitle() + onClickMarker.getSnippet();
		
		createDirectionsURL(furtherestMarker, markerMap.get(mapKey));
	}
	
	// Create an URL for sending to Google Directions Service. 
	public void createDirectionsURL(MyMarker furtherestMarker, MyMarker onClickMyMarker2) {

		String waypoints = "";
		for(MyMarker myMarker: markerArray) {
			if(!myMarker.equals(furtherestMarker) & !myMarker.equals(onClickMyMarker2)) {
				waypoints += "|" + myMarker.getLatitude() + "," + myMarker.getLongitude();
			}
		}
		
		String directionsUrl = "https://maps.googleapis.com/maps/api/directions/";
		
		directionsUrl += "json?origin=" + onClickMyMarker2.getLatitude() + "," + onClickMyMarker2.getLongitude();
		
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
		
		// Send the request in a separate thread.
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

		if(location != null) {
			// A nullPointerException would sometimes be thrown here for unknown reasons.
			try {	
				map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
						location.getLatitude(), location.getLongitude())));
			} catch (NullPointerException e) {
				// TODO: handle exception
			}
		}
	}
	
	
	// implement OnConnectionFailedListener interface
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		
	}
	
	// implement OnInfoWindowClickListener interface
	@Override
	public void onInfoWindowClick(Marker marker) {
		onClickMarker = marker;
		String mapKey = marker.getTitle() + marker.getSnippet();
		String stopID = Integer.toString(markerIDMap.get(mapKey));
		int id = Integer.parseInt(stopID);
		
		
		for (MyMarker myMarker: markerArray) {
			if(id == myMarker.getId()) {
				onClickMyMarker = myMarker;
			}
		}
		
		if (!onClickMyMarker.getHasBeenDelivered()) {
			Bundle myBundle = new Bundle();
			myBundle.putString("stopID", stopID);
			myBundle.putString("user", user);
			myBundle.putString("password", password);
			DeliveryDialogFragment dialog = new DeliveryDialogFragment();
			dialog.setArguments(myBundle);
			dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
		} 
			
	}
	
//	public void enableAndSaveMarker(int id) {
//		for(MyMarker myMarker: markerArray) {
//			if(id == myMarker.getId()) {
//				myMarker.setHasBeenDelivered(false);
//				
//				Gson gson = new Gson();
//				String  jsonString = gson.toJson(markerArray);
//				
//				Editor editor = savedValues.edit();
//				editor.putString("jsonString", jsonString);
//				editor.commit();
//			}
//		}
//		
//	}
	
	public void disableMarker(String dateTime, String delivered, String returns) {
		for (MyMarker myMarker: markerArray) {
			String snippet2 = myMarker.getAddress() + " " + myMarker.getCity();
			if(snippet2.equals(onClickMarker.getSnippet())) {
				myMarker.setHasBeenDelivered(true);
				myMarker.setDateTime(dateTime);
				myMarker.setDelivered(delivered);
				myMarker.setReturns(returns);
			}
		}
		LatLng position = onClickMarker.getPosition();
		String snippet = "Levert: " + delivered + 
          		" Retur: " + returns;
		String mapKey = dateTime + snippet;
		markerIDMap.put(mapKey, onClickMyMarker.getId());
		markerMap.put(mapKey, onClickMyMarker);

		map.clear();
		for(MyMarker mymarker: markerArray) {
			String snippet3 = mymarker.getAddress() + " "
					+ mymarker.getCity();
			if (!mymarker.getHasBeenDelivered()) {	
				map.addMarker(new MarkerOptions()
						.position(mymarker.getLatlng())
						.title(mymarker.getName())
						.snippet(snippet3));
			} else {
				String snippet2 = "Levert: " + mymarker.getDelivered() + 
             		" Retur: " + mymarker.getReturns();
				map.addMarker(new MarkerOptions()
                     .position(mymarker.getLatlng())
                     .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                     .title(mymarker.getDateTime())
                     .snippet(snippet2));
				
			}
		}
		 
		
	}
	
	// Save the markerArray to SharedPreferences as a String.
	public void saveMarkerArray() {
		
		Gson gson = new Gson();
		String  jsonString = gson.toJson(markerArray);
		
		Editor editor = savedValues.edit();
		editor.putString("jsonString", jsonString);
		editor.commit();
		
	}
	
	public void showErrorDialog(String result) {
		new AlertDialog.Builder(this)
		.setTitle("Noe gikk galt med opplastingen. Har mobilen nettverkstilgang?")
		.setMessage(result)
		.setCancelable(true)
		.setNegativeButton("OK", null)
        .show();
	}
	
	// Removes all the markers on the map, so a marker can be changed, and then all markers added again.
	// Changing a single marker on the map would sometimes not work correctly, an error in Googles code?
	public void resetMarkers() {
		map.clear();
		markerMap.clear();
		markerIDMap.clear();
		for(MyMarker myMarker: markerArray) {
			String snippet = myMarker.getAddress() + " "
					+ myMarker.getCity();
			String mapKey = myMarker.getName() + snippet;
			markerMap.put(mapKey, myMarker);
			markerIDMap.put(mapKey, myMarker.getId());
			myMarker.setHasBeenDelivered(false);
			map.addMarker(new MarkerOptions()
				.position(myMarker.getLatlng())
				.title(myMarker.getName())
				.snippet(snippet));
		}
	}

}
