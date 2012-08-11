package de.ifgi.fmt.parser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parser class for JSON strings that include information about flashmob signals
 * 
 * @author Matthias Robbers
 */
public class SignalJSONParser {
	public static String parse(String json) {
		String signal;
		try {
			JSONObject jsonObject = new JSONObject(json);
			signal = jsonObject.getString("type");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return signal;
	}
}
