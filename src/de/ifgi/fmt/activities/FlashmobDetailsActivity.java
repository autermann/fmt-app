package de.ifgi.fmt.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuItem;
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

public class FlashmobDetailsActivity extends SherlockMapActivity {
	// TODO: Change button text depending on user's participation status

	// TextViews
	private TextView fmTitleTV;
	private TextView fmIsPublicTV;
	private TextView fmParticipantsTV;
	private TextView fmDescriptionTV;
	private TextView fmAddressLineTv;
	private TextView fmLatitudeTv;
	private TextView fmLongitudeTv;

	// Button
	private Button participateButton;

	// Map stuff
	private MapView mapView = null;
	private MapController mapController;
	private GeoPoint fmLocation;

	// Flashmob and it's attributes
	private Flashmob flashmob;
	private double latitudeE6;
	private double longitudeE6;
	private String isPublicString;
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

		// Map stuff
		mapView = (MapView) findViewById(R.id.miniMapView);
		mapController = mapView.getController();
		fmLocation = new GeoPoint((int) latitudeE6, (int) longitudeE6);
		mapController.setCenter(fmLocation);
		mapController.setZoom(17);
		mapView.invalidate();
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources()
				.getDrawable(R.drawable.location);
		FlashmobItemizedOverlay fmOverlay = new FlashmobItemizedOverlay(
				drawable);
		OverlayItem overlayItem = new OverlayItem(fmLocation, "", "");
		fmOverlay.addOverlay(overlayItem);
		mapOverlays.add(fmOverlay);

		// TextViews
		fmTitleTV = (TextView) findViewById(R.id.fmTitleTV);
		fmIsPublicTV = (TextView) findViewById(R.id.fmIsPublicTV);
		fmParticipantsTV = (TextView) findViewById(R.id.fmParticipantsTV);
		fmDescriptionTV = (TextView) findViewById(R.id.fmDescriptionTV);
		fmAddressLineTv = (TextView) findViewById(R.id.fmAddressLineTV);
		fmLatitudeTv = (TextView) findViewById(R.id.fmLatitudeTV);
		fmLongitudeTv = (TextView) findViewById(R.id.fmLongitudeTV);

		participateButton = (Button) findViewById(R.id.openParticipateActivityButton);

		try {
			fillTextViews();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
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
				} else {
					// is participating: open ParticipateActivity
					if (flashmob.getSelectedRole() != null) {
						intent = new Intent(getApplicationContext(),
								ParticipateActivity.class);
						intent.putExtra("id", flashmob.getId());
					} else { // is not participating: send cancel request
						intent = new Intent(getApplicationContext(),
								ParticipateActivity.class);
						intent.putExtra("id", flashmob.getId());
					}
				}
				startActivity(intent);
			}
		});
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
		isPublicString = "Yes";
		if (!flashmob.isPublic()) {
			isPublicString = "No";
		}

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
		fmIsPublicTV.setText(isPublicString);
		fmParticipantsTV.setText(String.valueOf(flashmob.getParticipants()));
		fmDescriptionTV.setText(flashmob.getDescription());
		fmAddressLineTv.setText(addressLine + ", " + locality + ", " + country);
		fmLatitudeTv.setText("" + latitudeE6 / 1E6);
		fmLongitudeTv.setText("" + longitudeE6 / 1E6);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
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