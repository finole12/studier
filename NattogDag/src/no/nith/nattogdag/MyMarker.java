package no.nith.nattogdag;

import com.google.android.gms.maps.model.LatLng;

public class MyMarker {
	private double latitude;
	private double longitude;
	private String name;
	private String address;
	private String city;
	private String url;
	private LatLng latlng;
	
	private boolean hasBeenDelivered; //Toggles if this point has been market as delivered.
	private String delivered; //Delivered newspapers at this point
	private String returns; // Returned newspper at this point
	private String dateTime; // DateTime of delivery
	private int id;
	
	public MyMarker(String latitude, String longitude, String name, String address, String city,
			String id) {
		this.latitude = Double.parseDouble(latitude);
		this.longitude = Double.parseDouble(longitude);
		this.name = name;
		this.address = address;
		this.city = city;
		this.id = Integer.parseInt(id);
	}
	
	public MyMarker(Double latitude, Double longitude, String name, String address, String city,
			int id) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.address = address;
		this.city = city;
		this.id = id;
	}
	
	public MyMarker() {
		
	}
	

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public void setLatitude(String latitude) {
		this.latitude = Double.parseDouble(latitude);
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void setLongitude(String longitude) {
		this.longitude = Double.parseDouble(longitude);
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public LatLng getLatlng() {
		latlng = new LatLng(this.latitude, this.longitude);
		return latlng;
	}

	public void setLatlng(LatLng latlng) {
		this.latlng = latlng;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean getHasBeenDelivered() {
		return hasBeenDelivered;
	}

	public void setHasBeenDelivered(boolean hasBeenDelivered) {
		this.hasBeenDelivered = hasBeenDelivered;
	}

	public String getDelivered() {
		return delivered;
	}

	public void setDelivered(String delivered) {
		this.delivered = delivered;
	}

	public String getReturns() {
		return returns;
	}

	public void setReturns(String returns) {
		this.returns = returns;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	@Override
	public String toString() {
		String s = "" + latitude + "\n" + longitude + "\n" + name + "\n" + address + "\n" + url;
		
		return s;
		
	}

}
