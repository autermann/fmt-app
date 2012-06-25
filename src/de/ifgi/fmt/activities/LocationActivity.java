package de.ifgi.fmt.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.adapter.FlashmobListAdapter;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.network.NetworkRequest;
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.parser.FlashmobJSONParser;

public class LocationActivity extends SherlockActivity {
	LocationManager locationManager;
	LocationListener locationListener;
	Location currentLocation;
	double left, top, right, bottom;
	TextView locationText;

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
				currentLocation = location;
				Geocoder geocoder = new Geocoder(getApplicationContext());
				List<Address> addresses = null;
				try {
					addresses = geocoder.getFromLocation(
							location.getLatitude(), location.getLongitude(), 1);
					Address address = addresses.get(0);
					locationText = (TextView) findViewById(R.id.location_text);
					locationText.setText(address.getAddressLine(0));
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Remove the listener
				locationManager.removeUpdates(locationListener);
				new DownloadTask(LocationActivity.this).execute();
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

	private Location getDestination(Location start, double dist, double brng) {
		dist = dist / 6371.0;
		brng = Math.toRadians(brng);

		double lat1 = Math.toRadians(start.getLatitude());
		double lon1 = Math.toRadians(start.getLongitude());

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist)
				+ Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
		double a = Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1),
				Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
		double lon2 = lon1 + a;
		lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

		Location loc = new Location(LOCATION_SERVICE);
		loc.setLatitude(Math.toDegrees(lat2));
		loc.setLongitude(Math.toDegrees(lon2));
		return loc;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
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

	// AsyncTask instead of a Thread, in order to download the online data
	class DownloadTask extends AsyncTask<Void, Void, String> {
		ProgressDialog progressDialog;

		public DownloadTask(Context context) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Loading flashmobs...");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			// building bounding box
			double distance = 10; // (Kilometers)
			Location t = getDestination(currentLocation, distance, 0); // Top
			Location r = getDestination(currentLocation, distance, 90); // Right
			Location b = getDestination(currentLocation, distance, 180); // Bottom
			Location l = getDestination(currentLocation, distance, 270); // Left
			left = l.getLongitude();
			top = t.getLatitude();
			right = r.getLongitude();
			bottom = b.getLatitude();

			// sending server request
			String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs";
			url += "?bbox=" + bottom + "," + left + "," + top + "," + right;
			NetworkRequest request = new NetworkRequest(url);
			int result = request.send();
			if (result == NetworkRequest.NETWORK_PROBLEM) {
				return null;
			} else {
				return request.getResult();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				// parsing the result
				final ArrayList<Flashmob> flashmobs = FlashmobJSONParser.parse(
						result, getApplicationContext());
				// get access to the store and save the new flashmobs
				((Store) getApplicationContext()).setFlashmobs(flashmobs);

				// sort flashmobs by distance to current location
				Collections.sort(flashmobs, new Comparator<Flashmob>() {
					public int compare(Flashmob x, Flashmob y) {
						double dist1 = x
								.getDistanceInKilometersTo(currentLocation);
						double dist2 = y
								.getDistanceInKilometersTo(currentLocation);
						if (dist1 > dist2)
							return 1;
						else if (dist2 > dist1)
							return -1;
						else
							return 0;
					}
				});

				ListAdapter adapter = new FlashmobListAdapter(
						getApplicationContext(), flashmobs, currentLocation);
				ListView list = (ListView) findViewById(android.R.id.list);
				list.setAdapter(adapter);
				list.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Intent intent = new Intent(getApplicationContext(),
								FlashmobDetailsActivity.class);
						intent.putExtra("id", flashmobs.get(arg2).getId());
						startActivity(intent);
					}
				});
			} else {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
			progressDialog.dismiss();
		}

	}
}