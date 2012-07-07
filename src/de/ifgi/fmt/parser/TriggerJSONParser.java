package de.ifgi.fmt.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import de.ifgi.fmt.objects.Trigger;

public class TriggerJSONParser {
	public static Trigger parse(String json) {
		Trigger object = new Trigger();
		try {
			JSONObject jsonObject = new JSONObject(json);
			object.setId(jsonObject.getString("id"));
			if (jsonObject.has("description")) {
				object.setDescription(jsonObject.getString("description"));
			}
			if (jsonObject.has("time")) {
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				object.setTime(df.parse(jsonObject.getString("time")));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return object;
	}
}
