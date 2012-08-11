package de.ifgi.fmt.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

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
import android.util.Log;
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
import de.ifgi.fmt.data.PersistentStore;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.io.Flashmob;
import de.ifgi.fmt.io.Role;
import de.ifgi.fmt.parser.FlashmobJSONParser;
import de.ifgi.fmt.parser.RoleJSONParser;

public class LocationActivity extends SherlockActivity {
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location currentLocation;
	private double left, top, right, bottom;
	private TextView locationText;

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
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		} else {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		}
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
	class DownloadTask extends AsyncTask<Void, Void, ArrayList<Flashmob>> {
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
		protected ArrayList<Flashmob> doInBackground(Void... params) {
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

			// for testing, Jan is creating new flashmobs every five minutes
			// url += "&limit=10";

			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url);
				HttpResponse response = client.execute(request);
				String result = EntityUtils.toString(response.getEntity());
				// parsing the result
				final ArrayList<Flashmob> flashmobs = FlashmobJSONParser.parse(
						result, getApplicationContext());
				// get access to the store and save the new flashmobs
				Store store = (Store) getApplicationContext();
				for (Flashmob f : flashmobs) {
					if (store.hasFlashmob(f)) {
						Log.i("Store", "Flashmob is already in the store.");
					} else {
						// get selected Role
						if (PersistentStore.isMyFlashmob(
								getApplicationContext(), f)) {
							request = new HttpGet(
									"http://giv-flashmob.uni-muenster.de/fmt/users/"
											+ PersistentStore
													.getUserName(getApplicationContext())
											+ "/flashmobs/" + f.getId()
											+ "/role");
							Cookie cookie = PersistentStore
									.getCookie(getApplicationContext());
							request.setHeader("Cookie", cookie.getName() + "="
									+ cookie.getValue());
							response = client.execute(request);
							Log.i("URL", "" + request.getURI());
							Log.i("Status", "" + response.getStatusLine());
							result = EntityUtils.toString(response.getEntity());
							Role role = RoleJSONParser.parse(result,
									getApplicationContext());
							f.setSelectedRole(role);
						}
						// add to the temporal store
						store.addFlashmob(f);
						Log.i("Store", "Flashmob added to the store.");
					}
				}
				return flashmobs;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final ArrayList<Flashmob> flashmobs) {
			super.onPostExecute(flashmobs);
			if (flashmobs != null) {
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
						getApplicationContext(), flashmobs, currentLocation,
						true, false);
				ListView list = (ListView) findViewById(android.R.id.list);
				list.setAdapter(adapter);
				list.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// Password
						final String key = flashmobs.get(arg2).getKey();
						if (key != null
								&& flashmobs.get(arg2).getSelectedRole() == null) {
							new PasswordDialog(LocationActivity.this, flashmobs
									.get(arg2));
						} else {
							Intent intent = new Intent(getApplicationContext(),
									DetailsActivity.class);
							intent.putExtra("id", flashmobs.get(arg2).getId());
							startActivity(intent);
						}
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