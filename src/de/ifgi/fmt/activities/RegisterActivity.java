package de.ifgi.fmt.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.data.PersistentStore;
import de.ifgi.fmt.network.NetworkRequest;

public class RegisterActivity extends SherlockActivity {
	public static final int STATUS_NOT_VALID = 12;
	public static final int STATUS_OK = 11;

	public static final int INVALID_CREDENTIALS = 11;
	public static final int NO_INTERNET_CONNECTION = 99;

	private EditText username, password, password_rep, email;
	private Button register;
	private String userpassEncoded;

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

					if (username.getText().toString().length() < 4) {
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
			progressDialog.setMessage("Creating account and signing in...");
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
				return HttpStatus.SC_PROCESSING;
			}

			try {
				// Execute the Request
				HttpResponse response = httpclient.execute(httppost);
				Log.d("reg2", response.getStatusLine().toString());
				Log.d("reg3", response.getEntity().getContent().toString());
				if (HttpStatus.SC_CREATED != response.getStatusLine()
						.getStatusCode())
					return response.getStatusLine().getStatusCode();

				// / ab hier neuer absatz test zum direkten login:
				// /
				// /

				userpassEncoded = Base64.encodeToString((username.getText()
						+ ":" + password.getText()).getBytes("UTF-8"),
						Base64.NO_WRAP);
				DefaultHttpClient client = new DefaultHttpClient();
				String getURL = "http://giv-flashmob.uni-muenster.de/fmt/";
				HttpGet get = new HttpGet(getURL);
				get.setHeader("Authorization", "Basic " + userpassEncoded);
				response = client.execute(get);
				HttpEntity resEntityGet = response.getEntity();
				Log.i("wichtig", "Status: " + response.getStatusLine());
				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					return INVALID_CREDENTIALS;
				}

				resEntityGet.consumeContent();
				Log.i("wichtig", "Initial set of cookies:");
				List<Cookie> cookies = client.getCookieStore().getCookies();
				if (!cookies.isEmpty()) {
					PersistentStore.setUserName(getApplicationContext(),
							username.getText().toString());
					for (int i = 0; i < cookies.size(); i++) {
						if (cookies.get(i).getName()
								.equals(PersistentStore.KEY_COOKIE)) {
							PersistentStore.setCookie(getApplicationContext(),
									cookies.get(i).getValue());
						}
						Log.i("wichtig", "- " + cookies.get(i).toString());
					}
				}
				// My Flashmobs
				get = new HttpGet(
						"http://giv-flashmob.uni-muenster.de/fmt/users/"
								+ PersistentStore
										.getUserName(getApplicationContext())
								+ "/flashmobs");
				Cookie cookie = PersistentStore
						.getCookie(getApplicationContext());
				get.setHeader("Cookie",
						cookie.getName() + "=" + cookie.getValue());
				response = client.execute(get);
				

				// /
				// /
				// /
				// / bis hier hin ist der neue absatz test zum direkten login

				return response.getStatusLine().getStatusCode();

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				return NO_INTERNET_CONNECTION;
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
			case NO_INTERNET_CONNECTION:
				Toast.makeText(getApplicationContext(),
						"There is a problem with the Internet connection.",
						Toast.LENGTH_LONG).show();
				break;
			case HttpStatus.SC_PROCESSING:
				Toast.makeText(
						getApplicationContext(),
						"An encoding problem occurs. Please try again or ask the supprt.",
						Toast.LENGTH_LONG).show();
			case HttpStatus.SC_BAD_REQUEST:
				Toast.makeText(getApplicationContext(),
						"Error! Please try again or ask the supprt.",
						Toast.LENGTH_LONG).show();
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
		Intent intent = new Intent(this, StartActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

		Toast.makeText(this, "Thank you for registering " + username.getText() + ". Now you're logged in.",
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
