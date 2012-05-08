package de.ifgi.fmt.objects;

import com.google.android.maps.GeoPoint;

public class Flashmob
{
	// Attributes
	private String id;
	private String title;
	private GeoPoint location;
	private boolean isPublic;
	private int participants;
	private String description;

	// Constructor
	public Flashmob(String _id, String _title, GeoPoint _location, boolean _isPublic,
			int _participants, String _description)
	{
		this.id = _id;
		this.title = _title;
		this.location = _location;
		this.isPublic = _isPublic;
		this.participants = _participants;
		this.description = _description;
	}

	// Getter & Setter
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setLocation(GeoPoint location)
	{
		this.location = location;
	}

	public GeoPoint getLocation()
	{
		return location;
	}

	public boolean isPublic()
	{
		return isPublic;
	}

	public void setPublic(boolean isPublic)
	{
		this.isPublic = isPublic;
	}

	public int getParticipants()
	{
		return participants;
	}

	public void setParticipants(int participants)
	{
		this.participants = participants;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}
