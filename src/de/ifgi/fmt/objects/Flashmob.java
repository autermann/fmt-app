package de.ifgi.fmt.objects;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.location.Location;

import com.google.android.maps.GeoPoint;

public class Flashmob {
	// Attributes
	private String id;
	private String title;
	private GeoPoint location;
	private String country;
	private String city;
	private String streetAddress;
	private boolean isPublic;
	private int participants;
	private String description;
	private Date startTime;
	private Date endTime;
	private String href;
	private String key;
	private ArrayList<Role> roles;
	private Role selectedRole;

	// Constructors
	public Flashmob() {
	}

	public Flashmob(String _id, String _title, GeoPoint _location,
			boolean _isPublic, int _participants, String _description) {
		this.id = _id;
		this.title = _title;
		this.location = _location;
		this.isPublic = _isPublic;
		this.participants = _participants;
		this.description = _description;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
		return dateFormat.format(startTime);
	}

	public String getTime() {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.US);
		return dateFormat.format(startTime);
	}

	public double getDistanceInKilometersTo(Location location) {
		double latitude = getLocation().getLatitudeE6() / 1E6;
		double longitude = getLocation().getLongitudeE6() / 1E6;
		Location l = new Location("");
		l.setLatitude(latitude);
		l.setLongitude(longitude);
		float distanceInMeters = location.distanceTo(l);
		float distanceInKilometers = distanceInMeters / 1000;
		return distanceInKilometers;
	}

	// Getter & Setter
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public int getParticipants() {
		return participants;
	}

	public void setParticipants(int participants) {
		this.participants = participants;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public Role getSelectedRole() {
		return selectedRole;
	}

	public void setSelectedRole(Role selectedRole) {
		this.selectedRole = selectedRole;
	}

	public ArrayList<Role> getRoles() {
		return roles;
	}

	public void setRoles(ArrayList<Role> roles) {
		this.roles = roles;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
