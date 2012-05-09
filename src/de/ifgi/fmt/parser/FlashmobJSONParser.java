package de.ifgi.fmt.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

import de.ifgi.fmt.objects.Flashmob;

public class FlashmobJSONParser {
	public static ArrayList<Flashmob> parse(String json) {
		ArrayList<Flashmob> flashmobList = new ArrayList<Flashmob>();
		try {
			JSONArray flashmobs = new JSONArray(json);
			for (int i = 0; i < flashmobs.length(); i++) {
				JSONObject flashmob = flashmobs.getJSONObject(i);
				Flashmob f = new Flashmob();
				f.setId(flashmob.getString("id"));
				f.setTitle(flashmob.getString("title"));
				f.setPublic(flashmob.getBoolean("public"));
				f.setDescription(flashmob.getString("description"));
				JSONObject location = flashmob.getJSONObject("location");
				f.setLocation(new GeoPoint((int) (location
						.getDouble("latitude") * 1e6), (int) (location
						.getDouble("longitude") * 1e6)));
				flashmobList.add(f);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return flashmobList;
	}
}
