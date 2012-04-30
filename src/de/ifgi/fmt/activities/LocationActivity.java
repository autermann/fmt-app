package de.ifgi.fmt.activities;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;

public class LocationActivity extends SherlockActivity {
	LocationManager locationManager;
	LocationListener locationListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				LinearLayout waitingForLocation = (LinearLayout) findViewById(R.id.waiting_for_location);
				waitingForLocation.setVisibility(View.GONE);

				Geocoder geocoder = new Geocoder(getApplicationContext());
				List<Address> addresses = null;
				try {
					addresses = geocoder.getFromLocation(
							location.getLatitude(), location.getLongitude(), 1);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Address address = addresses.get(0);

				TextView addressText = (TextView) findViewById(R.id.address_text);
				TextView locationText = (TextView) findViewById(R.id.location_text);
				addressText.setText(address.getLocality() + ", "
						+ address.getCountryName());
				locationText.setText("Latitude: " + location.getLatitude()
						+ "\n" + "Longitude: " + location.getLongitude());
				// Remove the listener
				locationManager.removeUpdates(locationListener);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}