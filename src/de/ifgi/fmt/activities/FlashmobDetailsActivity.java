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
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.objects.Flashmob;

public class FlashmobDetailsActivity extends SherlockMapActivity
{
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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flashmob_details_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		try
		{
			getFlashmobData();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Map stuff
		mapView = (MapView) findViewById(R.id.miniMapView);
		mapController = mapView.getController();
		fmLocation = new GeoPoint((int) latitudeE6, (int) longitudeE6);
		mapController.animateTo(fmLocation);
		mapController.setZoom(17);
		mapView.invalidate();
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.location);
		FlashmobItemizedOverlay fmOverlay = new FlashmobItemizedOverlay(drawable);
		OverlayItem overlayItem = new OverlayItem(fmLocation, "", "");
		fmOverlay.addOverlay(overlayItem);
		mapOverlays.add(fmOverlay);

		// Button
		participateButton = (Button) findViewById(R.id.participateButton);
		participateButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO: ParticipateActivity �ffnen
				startParticipateActivity(v);
			}
		});

		// TextViews
		fmTitleTV = (TextView) findViewById(R.id.fmTitleTV);
		fmIsPublicTV = (TextView) findViewById(R.id.fmIsPublicTV);
		fmParticipantsTV = (TextView) findViewById(R.id.fmParticipantsTV);
		fmDescriptionTV = (TextView) findViewById(R.id.fmDescriptionTV);
		fmAddressLineTv = (TextView) findViewById(R.id.fmAddressLineTV);
		fmLatitudeTv = (TextView) findViewById(R.id.fmLatitudeTV);
		fmLongitudeTv = (TextView) findViewById(R.id.fmLongitudeTV);

		try
		{
			fillTextViews();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
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

	private void getFlashmobData() throws IOException
	{
		// Identify the flashmob
		Bundle extras = getIntent().getExtras();
		String id = extras.getString("id");

		// Get the flashmob
		flashmob = ((Store) getApplicationContext()).getFlashmobById(id);

		// Get the flashmob data
		latitudeE6 = flashmob.getLocation().getLatitudeE6();
		longitudeE6 = flashmob.getLocation().getLongitudeE6();
		isPublicString = "Yes";
		if (!flashmob.isPublic())
		{
			isPublicString = "No";
		}

		// Convert coordinates into an address
		Geocoder geocoder = new Geocoder(getApplicationContext());
		List<Address> list = geocoder.getFromLocation(latitudeE6 / 1E6, longitudeE6 / 1E6, 1);
		Address address = list.get(0);
		locality = address.getLocality();
		country = address.getCountryName();
		addressLine = address.getAddressLine(0);

		setTitle(flashmob.getTitle());
	}

	public void fillTextViews() throws IOException
	{
		fmTitleTV.setText(flashmob.getTitle());
		fmIsPublicTV.setText(isPublicString);
		// fmParticipantsTV.setText("");
		fmDescriptionTV.setText(flashmob.getDescription());
		fmAddressLineTv.setText(addressLine + ", " + locality + ", " + country);
		fmLatitudeTv.setText("" + latitudeE6 / 1E6);
		fmLongitudeTv.setText("" + longitudeE6 / 1E6);
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	public void startParticipateActivity(View v)
	{
		startActivity(new Intent(this, ParticipateActivity.class));
	}
}

class FlashmobItemizedOverlay extends ItemizedOverlay<OverlayItem>
{
	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();

	public FlashmobItemizedOverlay(Drawable defaultMarker)
	{
		super(boundCenterBottom(defaultMarker));
	}

	public void addOverlay(OverlayItem overlay)
	{
		overlays.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i)
	{
		return overlays.get(i);
	}

	@Override
	public int size()
	{
		return overlays.size();
	}
}