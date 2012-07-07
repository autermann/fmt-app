package de.ifgi.fmt.parser;

import org.json.JSONException;
import org.json.JSONObject;

import de.ifgi.fmt.objects.Activity;

public class ActivityJSONParser {
	public static Activity parse(String json) {
		Activity object = new Activity();
		try {
			JSONObject jsonObject = new JSONObject(json);
			object.setId(jsonObject.getString("id"));
			object.setTitle(jsonObject.getString("title"));
			object.setDescription(jsonObject.getString("description"));

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return object;
	}
}
