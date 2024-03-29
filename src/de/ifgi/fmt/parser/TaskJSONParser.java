package de.ifgi.fmt.parser;

import org.json.JSONException;
import org.json.JSONObject;

import de.ifgi.fmt.io.Task;

/**
 * Parser class for JSON strings that include information about flashmob tasks
 * 
 * @author Matthias Robbers
 */
public class TaskJSONParser {
	public static Task parse(String json) {
		Task object = new Task();
		try {
			JSONObject jsonObject = new JSONObject(json);
			object.setId(jsonObject.getString("id"));
			object.setDescription(jsonObject.getString("description"));
			if (jsonObject.has("href")) {
				object.setHref(jsonObject.getString("href"));
			}
			if (jsonObject.has("type")) {
				object.setType(jsonObject.getString("type"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return object;
	}
}
