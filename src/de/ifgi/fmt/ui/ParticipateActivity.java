package de.ifgi.fmt.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.adapter.RolesSpinnerAdapter;
import de.ifgi.fmt.data.PersistentStore;
import de.ifgi.fmt.data.Store;
import de.ifgi.fmt.io.Flashmob;
import de.ifgi.fmt.io.Role;

/**
 * Activity to choose a role of the flashmob and participate or cancel the
 * participation.
 * 
 * @author Sascha Koalick, Matthias Robbers
 */
public class ParticipateActivity extends SherlockActivity {
	private Button participateButton;
	private Flashmob flashmob;
	private Spinner roleSpinner;
	private ArrayList<Role> roles;
	private TextView roleDescriptionTv;
	private TextView roleItemsTv;

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
		roles = flashmob.getRoles();

		// Role Spinner
		roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
		RolesSpinnerAdapter adapter = new RolesSpinnerAdapter(
				getApplicationContext(), roles);
		roleSpinner.setAdapter(adapter);
		roleSpinner.setSelection(spinnerPos(), true);
		roleSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						Role r = (Role) parent.getItemAtPosition(pos);

						// Set the TextViews with the selected
						// role's
						// attributes
						roleDescriptionTv.setText(r.getDescription());
						roleItemsTv.setText(r.returnItemsAsString());
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
		setSpinnerStatus();

		// Set the TextViews with the attributes of the role the
		// spinner is set on
		roleDescriptionTv = (TextView) findViewById(R.id.roleDescriptionTv);
		roleDescriptionTv.setText(roles.get(spinnerPos()).getDescription());
		roleItemsTv = (TextView) findViewById(R.id.roleItemsTv);
		roleItemsTv.setText(roles.get(spinnerPos()).returnItemsAsString());

		// Participate button
		participateButton = (Button) findViewById(R.id.participateButton);
		setParticipateButtonLayout();
		participateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (flashmob.getSelectedRole() == null) {
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
							+ flashmob.getSelectedRole().getId()
							+ "/users/"
							+ PersistentStore
									.getUserName(getApplicationContext());
					new CancelTask(ParticipateActivity.this).execute(url);
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (flashmob.getSelectedRole() != null) {
			menu.add(0, DetailsActivity.MENU_PLAY, 0, "Start")
					.setIcon(R.drawable.ic_action_play)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_ALWAYS
									| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			intent = new Intent(this, StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case DetailsActivity.MENU_PLAY:
			intent = new Intent(this, ContentActivity.class);
			intent.putExtra("id", flashmob.getId());
			startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public int spinnerPos() {
		// If a role has been selected for this flashmob, get its Id
		// Otherwise set it to the Id of the first Role in the list of roles
		int spinnerPos = 0;

		if (flashmob.getSelectedRole() != null) {
			for (int i = 0; i < roles.size(); i++) {
				if (roles.get(i).getId()
						.equals(flashmob.getSelectedRole().getId())) {
					spinnerPos = i;
					break;
				}
			}
		}
		return spinnerPos;
	}

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
					+ PersistentStore.getUserName(getApplicationContext())
					+ "\"}";

			// HTTP POST Request to Server to register a user for a role
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url[0]);
			httppost.setHeader("Content-Type", "application/json");

			// Get cookie from SharedPrefs and add it to the header
			Cookie cookie = PersistentStore.getCookie(getApplicationContext());
			String name = cookie.getName();
			String value = cookie.getValue();
			httppost.setHeader("Cookie", name + "=" + value);

			try {
				httppost.setEntity(new StringEntity(jsonString));
				HttpResponse response = httpclient.execute(httppost);
				Log.i("URL", ": " + httppost.getURI());
				Log.i("Status", "" + response.getStatusLine());
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
				Role role = ((RolesSpinnerAdapter) roleSpinner.getAdapter())
						.getItem(roleSpinner.getSelectedItemPosition());
				flashmob.setSelectedRole(role);
				PersistentStore
						.addMyFlashmob(getApplicationContext(), flashmob);
				setParticipateButtonLayout();
				setSpinnerStatus();
				invalidateOptionsMenu();
				MyFlashmobsActivity.outdated = true;
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

				// Get cookie from SharedPrefs and add it to the header
				Cookie cookie = PersistentStore
						.getCookie(getApplicationContext());
				String name = cookie.getName();
				String value = cookie.getValue();
				httpdelete.setHeader("Cookie", name + "=" + value);

				HttpResponse response = httpclient.execute(httpdelete);

				Log.i("URL", "" + httpdelete.getURI());
				Log.i("Status", "" + response.getStatusLine());

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
				flashmob.setSelectedRole(null);
				PersistentStore.removeMyFlashmob(getApplicationContext(),
						flashmob);
				setParticipateButtonLayout();
				setSpinnerStatus();
				invalidateOptionsMenu();
				MyFlashmobsActivity.outdated = true;
			} else if (result == 0) {
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
			}
			progressDialog.dismiss();
		}

	}

	public void setSpinnerStatus() {
		if (flashmob.getSelectedRole() == null) {
			roleSpinner.setEnabled(true);
		} else {
			roleSpinner.setEnabled(false);
		}

	}

}
