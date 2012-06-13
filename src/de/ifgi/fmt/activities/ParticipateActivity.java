package de.ifgi.fmt.activities;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

import de.ifgi.fmt.R;

public class ParticipateActivity extends SherlockActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.participate_activity);
		setTitle("Participation");
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
	}

}
