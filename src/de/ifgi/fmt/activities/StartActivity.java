package de.ifgi.fmt.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;

public class StartActivity extends SherlockActivity {
	private static final int MENU_WEBSITE = 1;
	private static final int MENU_LOGIN = 2;
	private static final int MENU_LOGOUT = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
	}

	@Override
	protected void onResume() {
		super.onResume();
		invalidateOptionsMenu();
	}

	public void startMapActivity(View v) {
		startActivity(new Intent(this, MapActivity.class));
	}

	public void startLocationActivity(View v) {
		startActivity(new Intent(this, LocationActivity.class));
	}

	public void startAttributesActivity(View v) {
		startActivity(new Intent(this, AttributesActivity.class));
	}

	public void startMyFlashmobsActivity(View v) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (preferences.getInt("user_id", 0) == 0) {
			startActivity(new Intent(this, LoginActivity.class));
		} else {
			startActivity(new Intent(this, MyFlashmobsActivity.class));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_WEBSITE:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.google.com")));
			break;
		case MENU_LOGIN:
			startActivity(new Intent(this, LoginActivity.class).putExtra(
					"startActivity", LoginActivity.REDIRECT_TO_START_ACTIVITY));
			break;
		case MENU_LOGOUT:
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = preferences.edit();
			editor.remove("user_id");
			editor.remove("user_name");
			editor.remove("user_email");
			editor.commit();
			invalidateOptionsMenu();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		menu.add(0, MENU_WEBSITE, 0, "Website")
				.setIcon(R.drawable.ic_action_website)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_WITH_TEXT
								| MenuItem.SHOW_AS_ACTION_ALWAYS);
		if (preferences.getInt("user_id", 0) == 0) {
			menu.add(0, MENU_LOGIN, 0, "Login")
					.setIcon(R.drawable.ic_action_login)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_WITH_TEXT
									| MenuItem.SHOW_AS_ACTION_ALWAYS);
		} else {
			menu.add(0, MENU_LOGOUT, 0, "Logout").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_WITH_TEXT
							| MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return super.onCreateOptionsMenu(menu);
	}
}