package de.ifgi.fmt.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.data.PersistentStore;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.objects.Role;
import de.ifgi.fmt.parser.RoleJSONParser;

public class FlashmobDetailsActivity extends SherlockMapActivity {
	public static final int MENU_PLAY = 1;
	public static final int MENU_CALENDAR = 2;

	// TextViews
	private TextView fmTitleTV;
	private TextView fmIsPublicTV;
	private TextView fmParticipantsTV;
	private TextView fmDescriptionTV;
	private TextView fmAddressLineTv;
	private TextView fmLatitudeTv;
	private TextView fmLongitudeTv;
	private TextView fmDateTv;
	private TextView fmTimeTv;

	// Button
	private Button participateButton;

	// Map stuff
	private MapView mapView;
	private MapController mapController;
	private GeoPoint fmLocation;

	// Flashmob and it's attributes
	private Flashmob flashmob;
	private double latitudeE6;
	private double longitudeE6;
	private String addressLine;
	private String locality;
	private String country;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flashmob_details_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		getFlashmobData();

		setTitle(flashmob.getTitle());

		// TextViews
		fmTitleTV = (TextView) findViewById(R.id.fmTitleTV);
		fmIsPublicTV = (TextView) findViewById(R.id.fmIsPublicTV);
		fmParticipantsTV = (TextView) findViewById(R.id.fmParticipantsTV);
		fmDescriptionTV = (TextView) findViewById(R.id.fmDescriptionTV);
		fmAddressLineTv = (TextView) findViewById(R.id.fmAddressLineTV);
		fmLatitudeTv = (TextView) findViewById(R.id.fmLatitudeTV);
		fmLongitudeTv = (TextView) findViewById(R.id.fmLongitudeTV);
		fmDateTv = (TextView) findViewById(R.id.fmDateTV);
		fmTimeTv = (TextView) findViewById(R.id.fmTimeTV);
		fmLocation = new GeoPoint((int) latitudeE6, (int) longitudeE6);

		participateButton = (Button) findViewById(R.id.openParticipateActivityButton);

		mapView = (MapView) findViewById(R.id.miniMapView);
		mapController = mapView.getController();

		try {
			fillTextViews();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Map
		mapController.setCenter(fmLocation);
		mapController.setZoom(17);
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(
				R.drawable.marker_blue);
		FlashmobItemizedOverlay fmOverlay = new FlashmobItemizedOverlay(
				drawable);
		OverlayItem overlayItem = new OverlayItem(fmLocation, "", "");
		fmOverlay.addOverlay(overlayItem);
		mapOverlays.add(fmOverlay);

		((FrameLayout) findViewById(R.id.map_container))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getApplicationContext(),
								MapActivity.class);
						intent.putExtra("id", flashmob.getId());
						startActivity(intent);
					}
				});

		// Button
		setParticipateButtonLayout();
		participateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent;
				// if not logged in
				if (PersistentStore.getUserName(getApplicationContext()) == null) {
					intent = new Intent(getApplicationContext(),
							LoginActivity.class);
					startActivity(intent);
				} else {
					// is not participating: open ParticipateActivity
					if (flashmob.getSelectedRole() == null) {
						String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"
								+ flashmob.getId() + "/roles";
						new DownloadTask(FlashmobDetailsActivity.this)
								.execute(url);
					} else { // is participating: send cancel request
						// Unregister a user for a role
						String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"
								+ flashmob.getId()
								+ "/roles/"
								+ flashmob.getSelectedRole().getId()
								+ "/users/"
								+ PersistentStore
										.getUserName(getApplicationContext());
						new CancelTask(FlashmobDetailsActivity.this)
								.execute(url);
					}
				}
			}
		});
		invalidateOptionsMenu();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// reset MapView width and height
		mapView.getController().zoomIn();
		mapView.getController().zoomOut();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			intent = new Intent(this, StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case MENU_PLAY:
			intent = new Intent(this, ContentActivity.class);
			intent.putExtra("id", flashmob.getId());
			startActivity(intent);
		case MENU_CALENDAR:
			startActivity(createCalendarIntent());
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);
		menu.add(0, MENU_CALENDAR, 1, "+ Cal")
				.setIcon(R.drawable.ic_action_calendar)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		if (flashmob.getSelectedRole() != null) {
			menu.add(0, MENU_PLAY, 0, "Start")
					.setIcon(R.drawable.ic_action_play)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_ALWAYS
									| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		MenuItem actionItem = menu
				.findItem(R.id.menu_item_share_action_provider_action_bar);
		ShareActionProvider actionProvider = (ShareActionProvider) actionItem
				.getActionProvider();
		actionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		actionProvider.setShareIntent(createShareIntent());
		return super.onCreateOptionsMenu(menu);
	}

	private Intent createShareIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		// Add data to the intent, the receiving app will decide what to do with
		// it.
		intent.putExtra(Intent.EXTRA_SUBJECT, "Nice flashmob!");
		intent.putExtra(Intent.EXTRA_TEXT, "Hey, check out this flashmob \""
				+ flashmob.getTitle() + "\" I found on Flashmobber!");
		return intent;
	}

	private Intent createCalendarIntent() {
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("eventLocation", flashmob.getStreetAddress());
		intent.putExtra("description", flashmob.getDescription());
		intent.putExtra("beginTime", flashmob.getStartTime().getTime());
		intent.putExtra("endTime", flashmob.getStartTime().getTime() + 2 * 60
				* 60 * 1000);
		intent.putExtra("title", "Flashmob: " + flashmob.getTitle());
		return intent;
	}

	/**
	 * Set the layout of the Participate button depending on the user's
	 * participation status.
	 */
	public void setParticipateButtonLayout() {
		if (flashmob.getSelectedRole() != null) {
			participateButton.setText("Cancel Participation");
			participateButton
					.setBackgroundResource(R.drawable.cancel_button_background);
		} else {
			participateButton.setText("Participate");
			participateButton
					.setBackgroundResource(R.drawable.button_background);
		}
	}

	private void getFlashmobData() {
		// Get the flashmob
		flashmob = ((Store) getApplicationContext())
				.getFlashmobById(getIntent().getExtras().getString("id"));

		// Get the flashmob data
		latitudeE6 = flashmob.getLocation().getLatitudeE6();
		longitudeE6 = flashmob.getLocation().getLongitudeE6();

		// Convert coordinates into an address
		try {
			Geocoder geocoder = new Geocoder(getApplicationContext());
			List<Address> list;
			list = geocoder.getFromLocation(latitudeE6 / 1E6,
					longitudeE6 / 1E6, 1);
			Address address = list.get(0);
			locality = address.getLocality();
			country = address.getCountryName();
			addressLine = address.getAddressLine(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fills the TextViews with the flashmob's data.
	 * 
	 * @throws IOException
	 */
	public void fillTextViews() throws IOException {
		fmTitleTV.setText(flashmob.getTitle());
		if (flashmob.isPublic()) {
			fmIsPublicTV.setText("\u2714");
		} else {
			fmIsPublicTV.setText("\u2718");
		}
		fmParticipantsTV.setText(String.valueOf(flashmob.getParticipants()));
		fmDescriptionTV.setText(flashmob.getDescription());
		fmAddressLineTv.setText(addressLine + ", " + locality + ", " + country);
		fmLatitudeTv.setText("" + latitudeE6 / 1E6);
		fmLongitudeTv.setText("" + longitudeE6 / 1E6);
		fmDateTv.setText(flashmob.getDate());
		fmTimeTv.setText(flashmob.getTime());
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	class DownloadTask extends AsyncTask<String, Void, ArrayList<String>> {
		ProgressDialog progressDialog;

		public DownloadTask(Context context) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Loading roles...");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected ArrayList<String> doInBackground(String... url) {
			try {
				// Request for getting all roleIds
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url[0]);
				HttpResponse response;

				response = client.execute(request);
				String result = EntityUtils.toString(response.getEntity());

				Log.i("URL", "" + request.getURI());
				Log.i("Status", "" + response.getStatusLine());

				// roleIds
				ArrayList<String> roleIds = new ArrayList<String>();

				// Getting all roleIds

				try {
					JSONObject root = new JSONObject(result);
					JSONArray roles = root.getJSONArray("roles");

					for (int i = 0; i < roles.length(); i++) {
						roleIds.add(roles.getJSONObject(i).getString("id"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// Roles
				ArrayList<String> results = new ArrayList<String>();

				// Getting all roles
				for (String rId : roleIds) {
					// Request for every single roleId
					request = new HttpGet(
							"http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"
									+ flashmob.getId() + "/roles" + "/" + rId);
					response = client.execute(request);
					result = EntityUtils.toString(response.getEntity());
					results.add(result);
				}
				return results;
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<String> results) {
			super.onPostExecute(results);
			if (results != null) {
				ArrayList<Role> roles = new ArrayList<Role>();

				// Parsing all roles
				for (String result : results) {
					Role role = RoleJSONParser.parse(result,
							getApplicationContext());
					roles.add(role);
				}

				if (roles.size() > 0) {
					flashmob.setRoles(roles);
					Intent intent = new Intent(getApplicationContext(),
							ParticipateActivity.class);
					intent.putExtra("id", flashmob.getId());
					startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(),
							"No roles available for this flashmob, yet.",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}

			progressDialog.dismiss();
		}
	}

	class CancelTask extends AsyncTask<String, Void, Integer> {
		ProgressDialog progressDialog;

		public CancelTask(Context context) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Cancelling participation...");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... url) {
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpDelete httpdelete = new HttpDelete(url[0]);

				// Get cookie from SharedPrefs and add it to the header
				Cookie cookie = PersistentStore
						.getCookie(getApplicationContext());
				String name = cookie.getName();
				String value = cookie.getValue();
				httpdelete.setHeader("Cookie", name + "=" + value);

				HttpResponse response = httpclient.execute(httpdelete);

				Log.i("URL", "" + httpdelete.getURI());
				Log.i("Status", "" + response.getStatusLine());

				return response.getStatusLine().getStatusCode();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == HttpStatus.SC_NO_CONTENT) {
				flashmob.setSelectedRole(null);
				PersistentStore.removeMyFlashmob(getApplicationContext(),
						flashmob);
				setParticipateButtonLayout();
				invalidateOptionsMenu();
				MyFlashmobsActivity.outdated = true;
			} else if (result == 0) {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
			progressDialog.dismiss();
		}

	}

	class FlashmobItemizedOverlay extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();

		public FlashmobItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public void addOverlay(OverlayItem overlay) {
			overlays.add(overlay);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return overlays.get(i);
		}

		@Override
		public int size() {
			return overlays.size();
		}
	}

}