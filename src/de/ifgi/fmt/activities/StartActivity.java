package de.ifgi.fmt.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockActivity;

import de.ifgi.fmt.R;

public class StartActivity extends SherlockActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateLoginLogoutButton();
	}

	public void startMapActivity(View v) {
		startActivity(new Intent(this, MapActivity.class));
	}

	public void startLocationActivity(View v) {
		startActivity(new Intent(this, LocationActivity.class));
	}

	public void startNameActivity(View v) {
		// startActivity(new Intent(this, NameActivity.class));
		startActivity(new Intent(this, FlashmobListActivity.class));
	}

	public void startAttributesActivity(View v) {
		startActivity(new Intent(this, AttributesActivity.class));
	}

	public void startLoginActivity(View v) {
		startActivity(new Intent(this, LoginActivity.class).putExtra(
				"startActivity", LoginActivity.REDIRECT_TO_ACTIVITY_1));
	}

	public void startLogoutActivity(View v) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove("user_id");
		editor.remove("user_name");
		editor.remove("user_email");
		editor.commit();
		updateLoginLogoutButton();
	}

	private void updateLoginLogoutButton() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (preferences.getInt("user_id", 0) == 0) {
			((LinearLayout) findViewById(R.id.login_button))
					.setVisibility(View.VISIBLE);
			((LinearLayout) findViewById(R.id.logout_button))
					.setVisibility(View.GONE);
		} else {
			((LinearLayout) findViewById(R.id.login_button))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.logout_button))
					.setVisibility(View.VISIBLE);
		}
	}
}