package no.nith.nattogdag;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Directions {
	JSONObject jsonObject;
	JSONArray legsArray;
	String polylineEncoded;
	List<LatLng> polylineDecoded;
	double distance;
	String duration;
	

	public Directions(String jsonDirectionsResponse) {
		try {
			jsonObject = new JSONObject(jsonDirectionsResponse);
			JSONArray jsonArray = jsonObject.getJSONArray("routes");
			jsonObject = jsonArray.getJSONObject(0);
			JSONObject overview_polyline = jsonObject.getJSONObject("overview_polyline");
			legsArray = jsonObject.getJSONArray("legs");
			polylineEncoded = overview_polyline.getString("points");
			
		} catch (JSONException e) {
			Log.e("JSONException: ", e.getMessage());
		}	
		
		calculateRouteLengthAndDuration();	
	}
	
	/**
    * Method to decode polyline points
    * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
    * */
    private List<LatLng> decodePoly(String encoded) {
 
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
 
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
 
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
 
            LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
            poly.add(p);
        }
 
        return poly;
    }
    
    private void calculateRouteLengthAndDuration() {
    	int meters = 0;
    	int seconds = 0;
    	for(int i = 0; i < legsArray.length(); i++) {
    		try {
				JSONObject legX = legsArray.getJSONObject(i);
				JSONObject distanceLegX = legX.getJSONObject("distance");
				meters += distanceLegX.getInt("value");
				JSONObject durationLegX = legX.getJSONObject("duration");
				seconds += durationLegX.getInt("value");
			} catch (JSONException e) {
				Log.e("JSONException: ", e.getMessage());
			}
    	}
    	double kiloMeters = (double) meters;
    	kiloMeters = kiloMeters/1000;
    	distance = kiloMeters;
    	
    	int minutes = seconds / 60;
    	int hours = minutes / 60;
    	minutes = minutes % 60;
    	if (hours == 1) {
			duration = hours + " time" + ", ";
		} if (hours > 1) {
			duration = hours + " timer" + ", ";
		}
    	duration += minutes + " minutter";
    		
    }
    

	
	public String getPolylineEncoded() {
		return polylineEncoded;
	}

	public void setPolylineEncoded(String polylineEncoded) {
		this.polylineEncoded = polylineEncoded;
	}

	public List<LatLng> getPolylineDecoded() {
		return decodePoly(polylineEncoded);
	}

	public void setPolylineDecoded(List<LatLng> polylineDecoded) {
		this.polylineDecoded = polylineDecoded;
	}

	public double getDistance() {
		return distance;
	}

	public String getDuration() {
		return duration;
	}

}
