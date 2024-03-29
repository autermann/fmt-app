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

import de.ifgi.fmt.io.Flashmob;

/**
 * Parser class for JSON strings that include lists of flashmobs
 * 
 * @author Matthias Robbers
 */
public class FlashmobJSONParser {
	public static ArrayList<Flashmob> parse(String json, Context context) {
		ArrayList<Flashmob> flashmobList = new ArrayList<Flashmob>();
		try {
			JSONObject root = new JSONObject(json);
			JSONArray flashmobs = root.getJSONArray("flashmobs");
			for (int i = 0; i < flashmobs.length(); i++) {
				JSONObject flashmob = flashmobs.getJSONObject(i);
				Flashmob f = new Flashmob();
				f.setId(flashmob.getString("id"));
				f.setTitle(flashmob.getString("title"));
				f.setParticipants(flashmob.getInt("users"));
				f.setPublic(flashmob.getBoolean("public"));
				f.setHref(flashmob.getString("href"));
				if (flashmob.has("key"))
					f.setKey(flashmob.getString("key"));
				f.setHref(flashmob.getString("href"));
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				try {
					f.setStartTime(df.parse(flashmob.getString("startTime")));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				f.setDescription(flashmob.getString("description"));
				JSONArray coordinates = flashmob.getJSONObject("location")
						.getJSONArray("coordinates");
				f.setLocation(new GeoPoint(
						(int) (coordinates.getDouble(0) * 1e6),
						(int) (coordinates.getDouble(1) * 1e6)));
				Geocoder geocoder = new Geocoder(context);
				List<Address> addresses = null;
				try {
					addresses = geocoder.getFromLocation(f.getLocation()
							.getLatitudeE6() / 1E6, f.getLocation()
							.getLongitudeE6() / 1E6, 1);
					Address address = addresses.get(0);
					f.setCity(address.getLocality());
					f.setCountry(address.getCountryName());
					f.setStreetAddress(address.getAddressLine(0));
				} catch (IOException e) {
					e.printStackTrace();
				}
				flashmobList.add(f);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return flashmobList;
	}
}
