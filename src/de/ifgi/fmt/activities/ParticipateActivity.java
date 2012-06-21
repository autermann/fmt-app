package de.ifgi.fmt.activities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.adapter.RolesSpinnerAdapter;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.network.NetworkRequest;
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.objects.Role;
import de.ifgi.fmt.parser.RoleJSONParser;

public class ParticipateActivity extends SherlockActivity {
	private Button participateButton;
	private SharedPreferences prefs;
	private Flashmob flashmob;
	private String fId;
	private String PARTICIPATION_PREF_KEY;
	private Spinner roleSpinner;
	private ArrayList<Role> roles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.participate_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		// Initialize id
		fId = getFlashmobID();

		// This is a unique key string for saving the user's participation
		// status
		PARTICIPATION_PREF_KEY = fId + "Pref";

		// Initialize SharedPreferences
		prefs = SettingsActivity.getSettings(this);

		// Get the flashmob data
		getFlashmobData();

		setTitle(flashmob.getTitle());

		// Participate button
		participateButton = (Button) findViewById(R.id.participateButton);
		participateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isParticipating = prefs.getBoolean(
						PARTICIPATION_PREF_KEY, false);

				final SharedPreferences.Editor editor = prefs.edit();

				// Change button text depending on participation
				if (!isParticipating) {
					editor.putBoolean(PARTICIPATION_PREF_KEY, true);
					editor.commit();
					participateButton.setText("Cancel Participation");
					participateButton
							.setBackgroundResource(R.drawable.cancel_button_background);

					// TODO: Participate-Funktion hinzufügen
				} else {
					editor.putBoolean(PARTICIPATION_PREF_KEY, false);
					editor.commit();
					participateButton.setText("Participate");
					participateButton
							.setBackgroundResource(R.drawable.button_background);
				}
			}
		});

		String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs/" + fId
				+ "/roles";
		new DownloadTask(this).execute(url);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			// Intent intent = new Intent(getApplicationContext(),
			// FlashmobDetailsActivity.class);
			// intent.putExtra("id", id);
			// startActivity(intent);
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
	public String getFlashmobID() {
		// Identify the flashmob
		Bundle extras = getIntent().getExtras();

		// Get the ID
		String theID = extras.getString("id");

		return theID;
	}

	public void getFlashmobData() {
		// Get the flashmob
		flashmob = ((Store) getApplicationContext()).getFlashmobById(fId);
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
			NetworkRequest request;
			request = new NetworkRequest(url[0]);
			ArrayList<String> roleIds = new ArrayList<String>();
			int result = request.send();
			if (result == NetworkRequest.NETWORK_PROBLEM) {
				return null;
			} else {
				try {
					JSONObject root = new JSONObject(request.getResult());
					JSONArray roles = root.getJSONArray("roles");
					for (int i = 0; i < roles.length(); i++) {
						roleIds.add(roles.getJSONObject(i).getString("id"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			ArrayList<String> results = new ArrayList<String>();

			for (String rId : roleIds) {
				request = new NetworkRequest(
						"http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"+ fId +"/roles"
								+ "/" + rId);
				result = request.send();
				if (result == NetworkRequest.NETWORK_PROBLEM) {
					return null;
				} else {
					results.add(request.getResult());
				}
			}
			return results;
		}

		@Override
		protected void onPostExecute(ArrayList<String> results) {
			super.onPostExecute(results);
			if (results != null) {
				// parsing the results
				roles = new ArrayList<Role>();
				for (String result : results) {
					Role role = RoleJSONParser.parse(result,
							getApplicationContext());
					roles.add(role);
				}
				// adding the roles to the layout
				LinearLayout rolesList = (LinearLayout) findViewById(R.id.roles_list);
				TextView roleTitle;
				TextView roleDescription;
				for (Role r : roles) {
					roleTitle = new TextView(getApplicationContext());
					TypedValue tv = new TypedValue();
					if (getTheme().resolveAttribute(
							android.R.attr.textColorPrimary, tv, true)) {
						roleTitle.setTextColor(getApplicationContext()
								.getResources().getColor(tv.resourceId));
					}
					roleTitle.setTypeface(Typeface.DEFAULT_BOLD);
					roleTitle.setTextSize(16);
					roleTitle.setText(r.getTitle());
					roleDescription = new TextView(getApplicationContext());
					if (getTheme().resolveAttribute(
							android.R.attr.textColorSecondary, tv, true)) {
						roleDescription.setTextColor(getApplicationContext()
								.getResources().getColor(tv.resourceId));
					}
					roleDescription.setPadding(0, 0, 0, 16);
					roleDescription.setText(r.getDescription());
					rolesList.addView(roleTitle);
					rolesList.addView(roleDescription);

					// Role Spinner
					roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
					RolesSpinnerAdapter adapter = new RolesSpinnerAdapter(getApplicationContext(), roles);
					roleSpinner.setAdapter(adapter);
					roleSpinner
							.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
								public void onItemSelected(
										AdapterView<?> parent, View view,
										int pos, long id) {
									Role r = (Role) parent
											.getItemAtPosition(pos);
									Toast.makeText(getApplicationContext(),
											"Role ID: " + r.getId(),
											Toast.LENGTH_LONG).show();
								}

								@Override
								public void onNothingSelected(
										AdapterView<?> arg0) {
								}
							});
				}
				findViewById(R.id.participate_layout).setVisibility(
						View.VISIBLE);
			} else {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
			progressDialog.dismiss();
		}
	}
}
