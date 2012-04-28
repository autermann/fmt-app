package de.ifgi.fmt.classes;

public class Flashmob
{
	// Attributes
	private String id;
	private String title;
	private boolean isPublic;
	private int participants;
	private String description;
	
	// Constructor
	public Flashmob(String _id, String _title, boolean _isPublic, int _participants, String _description)
	{
		this.id = _id;
		this.title = _title;
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
