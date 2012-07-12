package de.ifgi.fmt.objects;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Location;

public class Trigger {
	private String id;
	private String description;
	private Location location;
	private Date time;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getDateAsString() {
		DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
		return dateFormat.format(time);
	}

	public String getTimeAsString() {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm aa", Locale.US);
		return dateFormat.format(time);
	}
}
