package de.ifgi.fmt.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import de.ifgi.fmt.io.Role;

public class RoleJSONParser
{
	public static Role parse(String json, Context context)
	{
		Role r = new Role();
		try
		{
			JSONObject role = new JSONObject(json);
			r.setId(role.getString("id"));
			r.setTitle(role.getString("title"));
			r.setDescription(role.getString("description"));
			r.setMinParticipants((role.getInt("minParticipants")));
			r.setMaxParticipants((role.getInt("maxParticipants")));
			
			// Items parsen
			JSONArray jsonItems = role.getJSONArray("items");
			String[] items = new String[jsonItems.length()];
			
			for (int i = 0; i < jsonItems.length(); i++)
			{
				String tempItem = jsonItems.getString(i);
				items[i] = tempItem;
			}
			r.setItems(items);
			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return r;
	}
}
