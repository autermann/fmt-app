package de.ifgi.fmt.activities;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;

public class FlashmobDetailsActivity extends SherlockActivity
{
	private TextView fmIdTV;
	private TextView fmTitleTV;
	private TextView fmIsPublicTV;
	private TextView fmParticipantsTV;
	private TextView fmDescriptionTV;
	private TextView fmAddressLineTv;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flashmob_details_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		fmIdTV = (TextView) findViewById(R.id.fmIdTV);
		fmTitleTV = (TextView) findViewById(R.id.fmTitleTV);
		fmIsPublicTV = (TextView) findViewById(R.id.fmIsPublicTV);
		fmParticipantsTV = (TextView) findViewById(R.id.fmParticipantsTV);
		fmDescriptionTV = (TextView) findViewById(R.id.fmDescriptionTV);
		fmAddressLineTv = (TextView) findViewById(R.id.fmAddressLineTV);

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
			Intent intent = new Intent(this, FlashmobListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void fillTextViews() throws IOException
	{
		Bundle extras = getIntent().getExtras();

		String id = extras.getString("id");
		String title = extras.getString("title");
		double latitude = extras.getInt("latitude")/1E6;
		double longitude = extras.getInt("longitude")/1E6;	
		boolean isPublic = extras.getBoolean("isPublic");
		int participants = extras.getInt("participants");
		String description = extras.getString("description");

		String isPublicString = "";
		if (isPublic)
		{
			isPublicString = "Yes";
		}
		else
		{
			isPublicString = "No";
		}

		// Convert coordinates into an address
		Geocoder geocoder = new Geocoder(getApplicationContext());
		List<Address> list = geocoder.getFromLocation(latitude, longitude, 1);
		Address address = list.get(0);
		String locality = address.getLocality();
		String country = address.getCountryName();
		String addressLine = address.getAddressLine(0);
		
		fmIdTV.setText("ID: " + id);
		fmTitleTV.setText("Title: " + title);
		fmIsPublicTV.setText("Is public: " + isPublicString);
		fmParticipantsTV.setText("Participants: " + participants);
		fmDescriptionTV.setText("Description: " + description);
		fmAddressLineTv.setText("Address: " + addressLine + ", " + locality + ", " + country);
	}
}
