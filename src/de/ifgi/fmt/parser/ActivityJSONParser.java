package de.ifgi.fmt.parser;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import de.ifgi.fmt.objects.Activity;

public class ActivityJSONParser {
	public static Activity parse(String json, Context context) {
		Activity object = new Activity();
		try {
			JSONObject Activity = new JSONObject(json);
			object.setId(Activity.getString("id"));
			object.setTitle(Activity.getString("title"));
			object.setDescription(Activity.getString("description"));

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}
}
