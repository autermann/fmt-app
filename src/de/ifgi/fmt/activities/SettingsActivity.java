package de.ifgi.fmt.activities;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

//		this.addPreferencesFromResource(R.xml.settings);

	}

	public static final SharedPreferences getSettings(final ContextWrapper ctxWrapper)
	{
		return ctxWrapper.getSharedPreferences(ctxWrapper.getPackageName() + "_preferences",
				MODE_PRIVATE);
	}
}
