package de.ifgi.fmt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockActivity;

import de.ifgi.fmt.R;

public class StartActivity extends SherlockActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
	}

	public void startMapActivity(View v) {
		startActivity(new Intent(this, MapActivity.class));
	}

	public void startLocationActivity(View v) {
		startActivity(new Intent(this, LocationActivity.class));
	}

	public void startNameActivity(View v) {
		startActivity(new Intent(this, NameActivity.class));
	}

	public void startAttributesActivity(View v) {
		startActivity(new Intent(this, AttributesActivity.class));
	}
}