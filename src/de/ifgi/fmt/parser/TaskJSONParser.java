package de.ifgi.fmt.parser;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import de.ifgi.fmt.objects.Task;

public class TaskJSONParser {
	public static Task parse(String json, Context context) {
		Task object = new Task();
		try {
			JSONObject Activity = new JSONObject(json);
			object.setId(Activity.getString("id"));
			object.setDescription(Activity.getString("description"));

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}
}
