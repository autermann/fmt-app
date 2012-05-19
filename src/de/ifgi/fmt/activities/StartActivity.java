package de.ifgi.fmt.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;

public class StartActivity extends SherlockActivity {
	private static final int MENU_LOGIN = 1;
	private static final int MENU_LOGOUT = 2;

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
		startActivity(new Intent(this, AttributesActivity.class));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_LOGIN:
			startActivity(new Intent(this, LoginActivity.class).putExtra(
					"startActivity", LoginActivity.REDIRECT_TO_ACTIVITY_1));
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
		if (preferences.getInt("user_id", 0) == 0) {
			menu.add(0, MENU_LOGIN, 1, "Login")
					.setIcon(R.drawable.ic_login)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_ALWAYS
									| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		} else {
			menu.add(0, MENU_LOGOUT, 1, "Logout")
					.setIcon(R.drawable.ic_logout)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_ALWAYS
									| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		return super.onCreateOptionsMenu(menu);
	}
}