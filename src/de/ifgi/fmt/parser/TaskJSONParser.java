package de.ifgi.fmt.parser;

import org.json.JSONException;
import org.json.JSONObject;

import de.ifgi.fmt.objects.Task;

public class TaskJSONParser {
	public static Task parse(String json) {
		Task object = new Task();
		try {
			JSONObject jsonObject = new JSONObject(json);
			object.setId(jsonObject.getString("id"));
			object.setDescription(jsonObject.getString("description"));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return object;
	}
}
