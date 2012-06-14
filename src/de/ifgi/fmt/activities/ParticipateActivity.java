package de.ifgi.fmt.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;

import de.ifgi.fmt.R;

public class ParticipateActivity extends SherlockActivity
{
	private Button participateButton;
	private SharedPreferences prefs;
	private String id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.participate_activity);
		setTitle("Participation");
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Get the flashmob's id
		Bundle extras = getIntent().getExtras();
		id = extras.getString("id");
		
		// This is a unique key string for saving if the user participates in the flashmob
		final String participationPrefKey = id + "Pref";
		
		// Shared preferences
		prefs = SettingsActivity.getSettings(this);

		// Participate button
		participateButton = (Button) findViewById(R.id.participateButton);
		participateButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				boolean isParticipating = prefs.getBoolean(participationPrefKey, false);
				
				final SharedPreferences.Editor editor = prefs.edit();

				// Change button text depending on participation
				if(!isParticipating)
				{
					editor.putBoolean(participationPrefKey, true);
					editor.commit();
					participateButton.setText("Cancel Participation");
					
					// TODO: Participate-Funktion hinzufügen
				}
				else				{
					editor.putBoolean(participationPrefKey, false);
					editor.commit();
					participateButton.setText("Participate");
				}
			}
		});
	}
}
