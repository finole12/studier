package no.nith.nattogdag;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

import android.util.Log;

public class Internet {
	
	public static String getJsonString(String urlString) {
		BufferedReader rd;
		String jsonString = ""; // <-- The JsonArray downloaded from server as a String.
		try {
			URL url = new URL(urlString);
			rd = new BufferedReader(
			        new InputStreamReader(url.openStream(), "ISO-8859-1"));
			Scanner scanner = new Scanner(rd);
			// Uses the regex \A, which matches the beginning of input, telling Scanner 
			// to tokenize the entire stream.
			jsonString = scanner.useDelimiter("\\A").next();
			
			//closing resources.
			scanner.close();
			rd.close();				
		} catch (Exception e) {
			Log.e("GetMarkers", e.toString());
		} 
		return jsonString;
	}

}
