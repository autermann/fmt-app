package de.ifgi.fmt.parser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.maps.GeoPoint;

import de.ifgi.fmt.objects.Flashmob;

public class FlashmobJSONParser
{
	public static ArrayList<Flashmob> parse(String json, Context context)
	{
		ArrayList<Flashmob> flashmobList = new ArrayList<Flashmob>();
		try
		{
			JSONArray flashmobs = new JSONArray(json);
			for (int i = 0; i < flashmobs.length(); i++)
			{
				JSONObject flashmob = flashmobs.getJSONObject(i);
				Flashmob f = new Flashmob();
				f.setId(flashmob.getString("id"));
				f.setTitle(flashmob.getString("title"));
				f.setPublic(flashmob.getBoolean("public"));
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try
				{
					f.setStartTime(df.parse(flashmob.getString("startTime")));
				}
				catch (ParseException e1)
				{
					e1.printStackTrace();
				}
				f.setDescription(flashmob.getString("description"));
				JSONObject location = flashmob.getJSONObject("location");
				f.setLocation(new GeoPoint((int) (location.getDouble("latitude") * 1e6),
						(int) (location.getDouble("longitude") * 1e6)));
				Geocoder geocoder = new Geocoder(context);
				List<Address> addresses = null;
				try
				{
					addresses = geocoder.getFromLocation(f.getLocation().getLatitudeE6() / 1E6, f
							.getLocation().getLongitudeE6() / 1E6, 1);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				Address address = addresses.get(0);
				f.setCity(address.getLocality());
				f.setCountry(address.getCountryName());
				f.setStreetAddress(address.getAddressLine(0));
				flashmobList.add(f);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return flashmobList;
	}
}
