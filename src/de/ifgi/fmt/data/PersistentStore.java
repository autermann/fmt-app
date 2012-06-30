package de.ifgi.fmt.data;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import de.ifgi.fmt.objects.Flashmob;

public class PersistentStore {
	public static String KEY_USER_NAME = "user_name";
	public static String KEY_COOKIE = "cookie";
	public static String KEY_MY_FLASHMOBS = "my_flashmobs";

	public static Cookie getCookie(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String name = "fmt_oid";
		String value = preferences.getString("fmt_oid", null);
		Cookie cookie = new BasicClientCookie(name, value);
		return cookie;
	}

	public static String getUserName(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getString(KEY_USER_NAME, null);
	}

	public static void setUserName(Context context, String userName) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = preferences.edit();
		editor.putString(KEY_USER_NAME, userName);
		editor.commit();
	}

	public static JSONArray getMyFlashmobs(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String myFlashmobs = preferences.getString(KEY_MY_FLASHMOBS, null);
		JSONArray array = new JSONArray();
		if (myFlashmobs != null) {
			try {
				array = new JSONArray(myFlashmobs);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return array;
	}

	public static void setMyFlashmobs(Context context, JSONArray array) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = preferences.edit();
		editor.putString(KEY_MY_FLASHMOBS, array.toString());
		editor.commit();
	}

	public static void addMyFlashmob(Context context, Flashmob flashmob) {
		JSONArray array = getMyFlashmobs(context);
		array.put(flashmob.getId());
		setMyFlashmobs(context, array);
	}

	public static void removeMyFlashmob(Context context, Flashmob flashmob) {
		try {
			JSONArray array = getMyFlashmobs(context);
			JSONArray newArray = new JSONArray();
			for (int i = 0; i < array.length(); i++) {
				if (!array.get(i).equals(flashmob.getId())) {
					newArray.put(array.get(i));
				}
			}
			setMyFlashmobs(context, newArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static boolean isMyFlashmob(Context context, Flashmob flashmob) {
		try {
			JSONArray array = getMyFlashmobs(context);
			for (int i = 0; i < array.length(); i++) {
				if (array.getString(i).equals(flashmob.getId())) {
					return true;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
}
