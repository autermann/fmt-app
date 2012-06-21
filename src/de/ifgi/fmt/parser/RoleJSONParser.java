package de.ifgi.fmt.parser;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import de.ifgi.fmt.objects.Role;

public class RoleJSONParser {
	public static Role parse(String json, Context context) {
		Role r = new Role();
		try {
			JSONObject role = new JSONObject(json);
			r.setId(role.getString("id"));
//			r.setTitle(role.getString("title"));
			r.setTitle("Role title");
			r.setDescription(role.getString("description"));
			r.setMinParticipants((role.getInt("minParticipants")));
			r.setMaxParticipants((role.getInt("maxParticipants")));

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return r;
	}
}
