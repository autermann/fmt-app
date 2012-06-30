package de.ifgi.fmt.activities;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import de.ifgi.fmt.objects.Flashmob;
import de.ifgi.fmt.objects.Role;
import de.ifgi.fmt.parser.RoleJSONParser;

public class ParticipateActivity extends SherlockActivity {
	private Button participateButton;
	private SharedPreferences prefs;
	private Flashmob flashmob;
	private Spinner roleSpinner;
	private ArrayList<Role> roles;
	private TextView roleDescriptionTv;
	private TextView roleItemsTv;
	private String selectedRoleId;
	private boolean isParticipating;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.participate_activity);

		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

		Store store = (Store) getApplicationContext();
		// Initialize flashmob
		flashmob = store.getFlashmobById(getIntent().getExtras()
				.getString("id"));
		setTitle(flashmob.getTitle());

		// Initialize SharedPreferences
		prefs = SettingsActivity.getSettings(this);

		// Get participation status
		if (flashmob.getSelectedRole() != null)
			isParticipating = true;
		else
			isParticipating = false;

		// Setting selectedRoleId to the roleId that the user registered for.
		// Else set it as null (important!).
		if (isParticipating)
			selectedRoleId = flashmob.getSelectedRole().getId();

		// Participate button
		participateButton = (Button) findViewById(R.id.participateButton);
		setParticipateButtonLayout();
		participateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isParticipating) {
					// Register a user for a role
					String selectedItem = ((RolesSpinnerAdapter) roleSpinner
							.getAdapter()).getItem(
							roleSpinner.getSelectedItemPosition()).getId();
					String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"
							+ flashmob.getId()
							+ "/roles/"
							+ selectedItem
							+ "/users";
					new UploadTask(ParticipateActivity.this).execute(url);
				} else {
					// Unregister a user for a role
					String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"
							+ flashmob.getId()
							+ "/roles/"
							+ selectedRoleId
							+ "/users/" + prefs.getString("user_name", "");
					new CancelTask(ParticipateActivity.this).execute(url);
				}
			}
		});

		String url = "http://giv-flashmob.uni-muenster.de/fmt/flashmobs/"
				+ flashmob.getId() + "/roles";
		new DownloadTask(this).execute(url);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(getApplicationContext(),
					FlashmobDetailsActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
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
		int spinnerPos = 0;

		if (isParticipating && selectedRoleId != null) {
			for (int i = 0; i < roles.size(); i++) {
				if (roles.get(i).getId().equals(selectedRoleId)) {
					spinnerPos = i;
					break;
				}
			}
		}
		return spinnerPos;
	}

	/**
	 * Set the layout of the Participate button depending on the user's
	 * participation status.
	 */
	public void setParticipateButtonLayout() {
		if (isParticipating) {
			participateButton.setText("Cancel Participation");
			participateButton
					.setBackgroundResource(R.drawable.cancel_button_background);
		} else {
			participateButton.setText("Participate");
			participateButton
					.setBackgroundResource(R.drawable.button_background);
		}
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

				Log.i("wichtig", "URL: " + request.getURI());
				Log.i("wichtig", "Status: " + response.getStatusLine());
				Log.i("wichtig", "Result: " + result);

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

					// In case the user did not use the spinner.
					// Otherwise it is set to the roleId that the use registered
					// for
					// in onCreate()
					if (selectedRoleId == null) {
						selectedRoleId = roles.get(0).getId();
					}

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

									// Set the TextViews with the selected
									// role's
									// attributes
									roleDescriptionTv.setText(r
											.getDescription());
									roleItemsTv.setText(r.returnItemsAsString());
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

	class UploadTask extends AsyncTask<String, Void, Integer> {
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
		protected Integer doInBackground(String... url) {

			// Build JSON-String to send to the server
			String jsonString = "{\"username\":\""
					+ prefs.getString("user_name", "") + "\"}";

			// HTTP POST Request to Server to register a user for a role
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url[0]);
			httppost.setHeader("Content-Type", "application/json");

			// Get cookie from SharedPrefs and add it to the header
			String name = "fmt_oid";
			String value = prefs.getString("fmt_oid", "");
			httppost.setHeader("Cookie", name + "=" + value);

			Log.i("wichtig", "URL: " + httppost.getURI());

			try {
				httppost.setEntity(new StringEntity(jsonString));
				HttpResponse response = httpclient.execute(httppost);
				Log.i("wichtig", "Status: " + response.getStatusLine());
				Log.i("wichtig",
						"Response: "
								+ EntityUtils.toString(response.getEntity()));
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
			if (result == HttpStatus.SC_CREATED) {
				isParticipating = true;
				// Change layout of the Participate/Cancel-Button
				participateButton.setText("Cancel Participation");
				participateButton
						.setBackgroundResource(R.drawable.cancel_button_background);
			} else if (result == 0) {
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

				// Create cookie from SharedPrefs and add it to the request's
				// header
				String name = "fmt_oid";
				String value = prefs.getString("fmt_oid", "");
				httpdelete.setHeader("Cookie", name + "=" + value);

				HttpResponse response = httpclient.execute(httpdelete);

				Log.i("wichtig", "URL: " + httpdelete.getURI());
				Log.i("wichtig", "Status: " + response.getStatusLine());

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
				isParticipating = false;
				// Change layout of the Participate/Cancel-Button
				participateButton.setText("Participate");
				participateButton
						.setBackgroundResource(R.drawable.button_background);
			} else if (result == 0) {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
			progressDialog.dismiss();
		}

	}

}
