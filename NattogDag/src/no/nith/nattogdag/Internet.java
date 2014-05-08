package no.nith.nattogdag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

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
	
	public static void sendDeliveryReport(String user, String password, String stopID, String delivered, 
			String returns) {
		BufferedReader rd;
		String jsonString = "";
		try {
			String parameters = URLEncoder.encode("command", "ISO-8859-1")
					+ "=" + URLEncoder.encode("deliveryReport", "ISO-8859-1");
					
					parameters += "&" + URLEncoder.encode("delivered", "ISO-8859-1")
							+ "=" + URLEncoder.encode(delivered, "ISO-8859-1");
					
					parameters += "&" + URLEncoder.encode("returns", "ISO-8859-1")
							+ "=" + URLEncoder.encode(returns, "ISO-8859-1");
					
					parameters += "&" + URLEncoder.encode("user", "ISO-8859-1")
							+ "=" + URLEncoder.encode(user, "ISO-8859-1");
					
					parameters += "&" + URLEncoder.encode("password", "ISO-8859-1")
							+ "=" + URLEncoder.encode(password, "ISO-8859-1");
					
					parameters += "&" + URLEncoder.encode("pointID", "ISO-8859-1")
							+ "=" + URLEncoder.encode(stopID, "ISO-8859-1");
				
//					String parameters = URLEncoder.encode("command", "ISO-8859-1")
//					+ "=" + URLEncoder.encode("getMarkers", "ISO-8859-1");
//					
//					parameters += "&" + URLEncoder.encode("user", "ISO-8859-1")
//							+ "=" + URLEncoder.encode(user, "ISO-8859-1");
//					
//					parameters += "&" + URLEncoder.encode("password", "ISO-8859-1")
//							+ "=" + URLEncoder.encode(password, "ISO-8859-1");
					
					URL url = new URL("https://nattogdagprosjekt-nith.rhcloud.com/NattogDag/JsonServlet");
					
					URLConnection conn = url.openConnection(); 
		            conn.setDoOutput(true); 
		            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
		            wr.write(parameters);
		            wr.flush();
		            
		            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		            
					Scanner scanner = new Scanner(rd);
					// Uses the regex \A, which matches the beginning of input, telling Scanner 
					// to tokenize the entire stream.
					String responseString = scanner.useDelimiter("\\A").next();
					scanner.close();
					Log.d("responseString", responseString);
		            
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.e("UnsupportedEncodingException", e.getMessage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Log.e("MalformedURLException", e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("IOException", e.toString());
		}
		
		
		
		
		
		
		
		
		
		
		
		
//		BufferedReader rd;		
//		HttpClient client = new DefaultHttpClient();
//		HttpHost targetHost = new HttpHost("https://nattogdagprosjekt-nith.rhcloud.com");
//		HttpPost post = new HttpPost("/NattogDag/JsonServlet");
//		
//		try {
//			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//			
//			pairs.add(new BasicNameValuePair("command", "deliveryReport"));
//			pairs.add(new BasicNameValuePair("delivered", delivered));
//			pairs.add(new BasicNameValuePair("returns", returns));
//			pairs.add(new BasicNameValuePair("user", user));
//			pairs.add(new BasicNameValuePair("password", password));
//			pairs.add(new BasicNameValuePair("stopID", stopID));
//			post.setEntity(new UrlEncodedFormEntity(pairs));
//			
//			HttpResponse response = client.execute(targetHost, post);
//			rd = new BufferedReader(
//			        new InputStreamReader(response.getEntity().getContent()));
//			Scanner scanner = new Scanner(rd);
//			// Uses the regex \A, which matches the beginning of input, telling Scanner 
//			// to tokenize the entire stream.
//			String responseString = scanner.useDelimiter("\\A").next();
//			scanner.close();
//			Log.d("responseString", responseString);
//			
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			Log.e("UnsupportedEncodingException", e.getMessage());
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			Log.e("ClientProtocolException", e.getMessage());
//		} catch (IOException e) {
//			Log.e("IOException", e.toString());
//			
//		}
	}

}
