package de.ifgi.fmt.objects;

public class Point
{
	private double latitude;
	private double longitude;

	public Point(double _latitude, double _longitude)
	{
		this.setLatitude(_latitude);
		this.setLongitude(_longitude);
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

}
