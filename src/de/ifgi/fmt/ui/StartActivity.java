package de.ifgi.fmt.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.data.PersistentStore;
import de.ifgi.fmt.data.Store;

/**
 * Start screen of the application that gives access to the main features.
 * 
 * @author Matthias Robbers
 */
public class StartActivity extends SherlockActivity {
	private static final int MENU_WEBSITE = 1;
	private static final int MENU_LOGIN = 2;
	private static final int MENU_LOGOUT = 3;
	private static final int MENU_ABOUT = 4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
	}

	@Override
	protected void onResume() {
		super.onResume();
		invalidateOptionsMenu();

		Log.i("User", "" + PersistentStore.getUserName(this));
		Log.i("Cookie", "" + PersistentStore.getCookie(this).getValue());
		Log.i("Persistent Flashmobs", "" + PersistentStore.getMyFlashmobs(this));
	}

	public void startMapActivity(View v) {
		startActivity(new Intent(this, MapActivity.class));
	}

	public void startLocationActivity(View v) {
		startActivity(new Intent(this, NearbyActivity.class));
	}

	public void startAttributesActivity(View v) {
		startActivity(new Intent(this, AttributesActivity.class));
	}

	public void startMyFlashmobsActivity(View v) {
		if (PersistentStore.getUserName(this) == null) {
			startActivity(new Intent(this, LoginActivity.class).putExtra(
					"redirectTo", "de.ifgi.fmt.activities.MyFlashmobsActivity"));
		} else {
			startActivity(new Intent(this, MyFlashmobsActivity.class));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_WEBSITE:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://giv-flashmob.uni-muenster.de/hp/")));
			break;
		case MENU_LOGIN:
			startActivity(new Intent(this, LoginActivity.class));
			break;
		case MENU_LOGOUT:
			// remove everything from temporal and persistent storage
			((Store) getApplicationContext()).clear();
			PersistentStore.clear(getApplicationContext());
			invalidateOptionsMenu();
			break;
		case MENU_ABOUT:
			new AboutDialog(this);
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (preferences.getString("user_name", null) == null) {
			menu.add(0, MENU_LOGIN, 0, "Login")
					.setIcon(R.drawable.ic_action_login)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_WITH_TEXT
									| MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(0, MENU_WEBSITE, 0, "Website")
					.setIcon(R.drawable.ic_action_website)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_IF_ROOM
									| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		} else {
			menu.add(0, MENU_WEBSITE, 0, "Website")
					.setIcon(R.drawable.ic_action_website)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_WITH_TEXT
									| MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(0, MENU_LOGOUT, 0, "Logout").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		menu.add(0, MENU_ABOUT, 0, "About").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_NEVER);
		return super.onCreateOptionsMenu(menu);
	}
}