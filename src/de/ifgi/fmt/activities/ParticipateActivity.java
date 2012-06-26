package de.ifgi.fmt.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
	private String ROLE_ID_PREF_KEY;
	private Spinner roleSpinner;
	private ArrayList<Role> roles;
	private TextView roleDescriptionTv;
	private TextView roleItemsTv;
	private String selectedRoleId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.participate_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		// Initialize id
		fId = getFlashmobID();

		// Key string for saving the user's participation status
		PARTICIPATION_PREF_KEY = fId + "ParticipationPref";

		// Key string for saving selected roleId
		ROLE_ID_PREF_KEY = fId + "RoleIdPref";

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

				// Change button text depending on participation status
				if (!isParticipating) {
					editor.putBoolean(PARTICIPATION_PREF_KEY, true);
					editor.commit();
					participateButton.setText("Cancel Participation");
					participateButton
							.setBackgroundResource(R.drawable.cancel_button_background);

					// Save roleId in SharedPreferences
					editor.putString(ROLE_ID_PREF_KEY, selectedRoleId);
					editor.commit();

					// TODO: Add Participate-Funktion
					// Register a user for a role
					String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"
							+ fId + "/roles/" + selectedRoleId + "/users";
					new UploadTask(ParticipateActivity.this).execute(url);
				} else {
					editor.putBoolean(PARTICIPATION_PREF_KEY, false);
					editor.commit();
					participateButton.setText("Participate");
					participateButton
							.setBackgroundResource(R.drawable.button_background);

					// TODO: Add Cancel-Participate-Funktion
					// Unregister a user for a role
					// Wichtig: Rolle canceln, die in SharedPrefs gespeichert
					// ist
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

	/**
	 * Initializes the flashmob with its data.
	 */
	public void getFlashmobData() {
		// Get the flashmob
		flashmob = ((Store) getApplicationContext()).getFlashmobById(fId);
	}

	/**
	 * Returns an int to set the role spinner to the position of the saved
	 * selected role.
	 * 
	 * @return
	 */
	public int spinnerPos() {
		// If a role has been selected for this flashmob, get its Id
		// Otherwise set it to the Id of the first Role in the list of roles
		// String tempRoleId = prefs.getString(ROLE_ID_PREF_KEY, roles.get(0)
		// .getId());

		int spinnerPos = 0;

		// for (int i = 0; i < roles.size(); i++) {
		// if (roles.get(i).getId().equals(tempRoleId)) {
		// spinnerPos = i;
		// break;
		// }
		// }

		return spinnerPos;
	}

	class DownloadTask extends AsyncTask<String, Void, ArrayList<String>> {
		// Is shown when the activity starts and while downloading the roles
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
			// Request for getting all roleIds
			NetworkRequest request = new NetworkRequest(url[0]);

			// roleIds
			ArrayList<String> roleIds = new ArrayList<String>();

			int result = request.send();

			// Getting all roleIds
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

			// Roles
			ArrayList<String> results = new ArrayList<String>();

			// Getting all roles
			for (String rId : roleIds) {
				// Request for every single roleId
				request = new NetworkRequest(
						"http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"
								+ fId + "/roles" + "/" + rId);
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
				roles = new ArrayList<Role>();

				// Parsing all roles
				for (String result : results) {
					Role role = RoleJSONParser.parse(result,
							getApplicationContext());
					roles.add(role);
				}

				if (roles.size() > 0) {
					// Set the TextViews with the attributes of the role the
					// spinner
					// is set on
					roleDescriptionTv = (TextView) findViewById(R.id.roleDescriptionTv);
					roleDescriptionTv.setText(roles.get(spinnerPos())
							.getDescription());
					roleItemsTv = (TextView) findViewById(R.id.roleItemsTv);
					roleItemsTv.setText(roles.get(spinnerPos())
							.returnItemsAsString());

					// Role Spinner
					roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
					RolesSpinnerAdapter adapter = new RolesSpinnerAdapter(
							getApplicationContext(), roles);
					roleSpinner.setAdapter(adapter);
					roleSpinner.setSelection(spinnerPos(), true);
					roleSpinner
							.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
								public void onItemSelected(
										AdapterView<?> parent, View view,
										int pos, long id) {
									Role r = (Role) parent
											.getItemAtPosition(pos);
									// Toast.makeText(getApplicationContext(),
									// "Role ID: " + r.getId(),
									// Toast.LENGTH_LONG).show();

									// Set the TextViews with the selected
									// role's
									// attributes
									roleDescriptionTv.setText(r
											.getDescription());
									roleItemsTv.setText(r.returnItemsAsString());

									// Save roleId to "remember" the selected
									// role
									selectedRoleId = r.getId();
								}

								@Override
								public void onNothingSelected(
										AdapterView<?> arg0) {
									// Do nothing
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

	class UploadTask extends AsyncTask<String, Void, Void> {
		// Is shown while the activity is uploading the participation data
		ProgressDialog progressDialog;

		public UploadTask(Context context) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Uploading participation status...");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... url) {
			// Get saved roleId from SharedPrefs
			String roleId = prefs.getString(ROLE_ID_PREF_KEY, selectedRoleId);

			// Build JSON-String to send to the server
			String jsonString = "{\"id\":\"" + roleId + "\"}";

			// Create a local instance of cookie store
			CookieStore cookieStore = new BasicCookieStore();

			// Create cookie from SharedPrefs and add it to the cookie store
			String name = "fmt_oid";
			String value = prefs.getString("fmt_oid", "");
			Cookie cookie = new BasicClientCookie(name, value);
			cookieStore.addCookie(cookie);

			// Create local HTTP context
			HttpContext localContext = new BasicHttpContext();

			// Bind cookie store to the local context
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			// HTTP POST Request to Server to register a user for a role
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url[0]);
			httppost.setHeader("Content-Type", "application/json");
			
			Log.d("wichtig", "URL: "+ url[0]);
			
			try {
				httppost.setEntity(new StringEntity(jsonString));
				Log.d("participation1", httppost.toString());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			try {
				HttpResponse response = httpclient.execute(httppost,
						localContext);
				Log.i("wichtig", "Status: "+ response.getStatusLine());
				Log.i("wichtig", "Response: "+ EntityUtils.toString(response.getEntity()));
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			

			// Ist return null wirklich richtig hier???
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			progressDialog.dismiss();
		}

	}

}
