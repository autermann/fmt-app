package de.ifgi.fmt.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.objects.Flashmob;

public class ParticipateActivity extends SherlockActivity
{
	private Button participateButton;
	private SharedPreferences prefs;
	private Flashmob flashmob;
	private String id;
	private String PARTICIPATION_PREF_KEY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.participate_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Initialize id
		id = getFlashmobID();
		
		// This is a unique key string for saving the user's participation status
		PARTICIPATION_PREF_KEY = id + "Pref";
		
		// Initialize SharedPreferences
		prefs = SettingsActivity.getSettings(this);

		// Get the flashmob data
		getFlashmobData();
		
		setTitle(flashmob.getTitle());
		
		// Participate button
		participateButton = (Button) findViewById(R.id.participateButton);
		participateButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				boolean isParticipating = prefs.getBoolean(PARTICIPATION_PREF_KEY, false);
				
				final SharedPreferences.Editor editor = prefs.edit();

				// Change button text depending on participation
				if(!isParticipating)
				{
					editor.putBoolean(PARTICIPATION_PREF_KEY, true);
					editor.commit();
					participateButton.setText("Cancel Participation");
					
					// TODO: Participate-Funktion hinzufügen
				}
				else				
				{
					editor.putBoolean(PARTICIPATION_PREF_KEY, false);
					editor.commit();
					participateButton.setText("Participate");
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			onBackPressed();
//			Intent intent = new Intent(getApplicationContext(), FlashmobDetailsActivity.class);
//			intent.putExtra("id", id);
//			startActivity(intent);
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Gets the flashmob's id from the intent that is starting this Activity.
	 * 
	 * @return
	 */
	public String getFlashmobID()
	{
		// Identify the flashmob
		Bundle extras = getIntent().getExtras();
		
		// Get the ID
		String theID = extras.getString("id");
		
		return theID;
	}
	
	public void getFlashmobData()
	{
		// Get the flashmob
		flashmob = ((Store) getApplicationContext()).getFlashmobById(id);
	}
}
