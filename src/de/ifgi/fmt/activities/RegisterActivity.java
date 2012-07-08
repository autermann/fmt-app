package de.ifgi.fmt.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.network.NetworkRequest;

public class RegisterActivity extends SherlockActivity {
	public static final int STATUS_NOT_VALID = 12;
	public static final int STATUS_OK = 11;

	public static final int INVALID_CREDENTIALS = 11;

	private EditText username, password, password_rep, email;
	private Button register;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		setTitle("Register");
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		username = (EditText) findViewById(R.id.username);

		// The input fields & buttons
		username.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		password = (EditText) findViewById(R.id.password);
		password_rep = (EditText) findViewById(R.id.password_rep);

		email = (EditText) findViewById(R.id.email);
		email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

		register = (Button) findViewById(R.id.register);
		register.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				registering();
			}
		});
	}

	private void registering() {
		// checking if the first two input fields are not empty, email is
		// optional
		if (username.getText().toString().equals("")
				|| password.getText().toString().equals("")
				|| password_rep.getText().toString().equals("")) {
			Toast.makeText(
					getApplicationContext(),
					"Please enter a valid user name and password. One mandatory field is empty.",
					Toast.LENGTH_LONG).show();

		} else {

			if (!password.getText().toString()
					.equals(password_rep.getText().toString())) {
				Toast.makeText(getApplicationContext(),
						"The passwords do not match, please try again",
						Toast.LENGTH_LONG).show();
			} else {

				if (password.getText().toString().length() < 8) {
					Toast.makeText(
							getApplicationContext(),
							"The password is too short. It has to contain at least 8 characters.",
							Toast.LENGTH_LONG).show();
				} else {
					
					if(username.getText().toString().length() < 4){
						Toast.makeText(
								getApplicationContext(),
								"The username is too short. It has to contain at least 4 characters.",
								Toast.LENGTH_LONG).show();
					} else {
						
						// if everything is okay, create new account
						new RegisterTask(this).execute();
					}

				}
			}
		}
	}

	// AsyncTask for the creation of the Registering process
	class RegisterTask extends AsyncTask<String, Void, Integer> {
		ProgressDialog progressDialog;

		public RegisterTask(Context context) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Registering...");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... url) {
			// HTTP POST Request to Server to create new Account
			// URL and Content-Type have to be defined
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://giv-flashmob.uni-muenster.de/fmt/users");
			httppost.setHeader("Content-Type", "application/json");

			try {
				// JSON-Code for creating a new user
				// Combination of predefined JSON + input fields
				if (email.getText().toString().equals("")) {
					httppost.setEntity(new StringEntity("{\"username\":\""
							+ username.getText().toString()
							+ "\",\"password\":\""
							+ password.getText().toString() + "\"}"));
				} else {
					httppost.setEntity(new StringEntity("{\"username\":\""
							+ username.getText().toString()
							+ "\",\"password\":\""
							+ password.getText().toString() + "\",\"email\":\""
							+ email.getText().toString() + "\"}"));
				}

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				// Execute the Request
				HttpResponse response = httpclient.execute(httppost);
				Log.d("reg2", response.getStatusLine().toString());
				Log.d("reg3", response.getEntity().getContent().toString());
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return 0;
		}

		@Override
		// If something doesn't work... Catching the main problems
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			switch (result) {
			case NetworkRequest.NETWORK_PROBLEM:
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
				break;
			case INVALID_CREDENTIALS:
				Toast.makeText(getApplicationContext(),
						"This username already exists", Toast.LENGTH_LONG)
						.show();
				password.setText("");
				break;
			default:
				redirect();
				break;
			}
			progressDialog.dismiss();
		}

	}

	// After registering, the app redirects to the start screen and shows a
	// Toast
	public void redirect() {
		Intent intent = null;

		intent = new Intent(this, StartActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

		Toast.makeText(this, "Thank you for registering " + username.getText(),
				Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
}
