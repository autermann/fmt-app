package de.ifgi.fmt.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.ifgi.fmt.R;
import de.ifgi.fmt.network.NetworkRequest;

public class LoginActivity extends SherlockActivity {
	public static final int STATUS_NOT_VALID = 12;
	public static final int STATUS_OK = 11;

	public static final int INVALID_CREDENTIALS = 11;

	public static final int REDIRECT_TO_START_ACTIVITY = 1;
	public static final int REDIRECT_TO_MY_FLASHMOBS_ACTIVITY = 2;
	// ...

	private EditText username, password;
	private Button login, register;
	private String userpassEncoded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		setTitle("Login");
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		username = (EditText) findViewById(R.id.username);

		// already in XML, specially for HTC in Java
		username.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		password = (EditText) findViewById(R.id.password);
		password.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == 0) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
					login.performClick();
				}
				return false;
			}
		});

		login = (Button) findViewById(R.id.login);
		login.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				authenticate();
			}
		});
		
		register = (Button) findViewById(R.id.register);
		register.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
				LoginActivity.this.startActivity(myIntent);
				
			}
		});
	}

	private void authenticate() {
		if (username.getText().toString().equals("")
				|| password.getText().toString().equals("")) {
			Toast.makeText(getApplicationContext(),
					"Please enter user name and password.", Toast.LENGTH_LONG)
					.show();
		} else {
			new LoginTask(this).execute();
		}
	}

	class LoginTask extends AsyncTask<String, Void, Integer> {
		ProgressDialog progressDialog;

		public LoginTask(Context context) {
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Authenticating...");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... url) {
			try {
				userpassEncoded = Base64.encodeToString((username.getText()
						+ ":" + password.getText()).getBytes("UTF-8"),
						Base64.NO_WRAP);
				DefaultHttpClient client = new DefaultHttpClient();
				String getURL = "http://giv-flashmob.uni-muenster.de/fmt/";
				HttpGet get = new HttpGet(getURL);
				get.setHeader("Authorization", "Basic " + userpassEncoded);
				HttpResponse responseGet = client.execute(get);
				HttpEntity resEntityGet = responseGet.getEntity();
				Log.i("wichtig", "Status: " + responseGet.getStatusLine());
				if (responseGet.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					return INVALID_CREDENTIALS;
				}
				if (resEntityGet != null) {
					resEntityGet.consumeContent();
					Log.i("wichtig", "Initial set of cookies:");
					List<Cookie> cookies = client.getCookieStore().getCookies();
					if (!cookies.isEmpty()) {
						SharedPreferences preferences = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						SharedPreferences.Editor editor = preferences.edit();
						editor.putString("user_name", username.getText()
								.toString());
						for (int i = 0; i < cookies.size(); i++) {
							editor.putString(cookies.get(i).getName(), cookies
									.get(i).getValue());
							Log.i("wichtig", "- " + cookies.get(i).toString());
						}
						editor.commit();
					}
					return 1;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				return NetworkRequest.NETWORK_PROBLEM;
			}
			return 0;
		}

		@Override
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
						"Username and password do not match.",
						Toast.LENGTH_LONG).show();
				password.setText("");
				break;
			default:
				redirect();
				break;
			}
			progressDialog.dismiss();
		}

	}

	public void redirect() {
		Intent intent = null;
		switch (getIntent().getExtras().getInt("startActivity")) {
		case REDIRECT_TO_START_ACTIVITY:
			intent = new Intent(this, StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			break;
		case REDIRECT_TO_MY_FLASHMOBS_ACTIVITY:
			intent = new Intent(this, StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			break;
		}
		startActivity(intent);
		Toast.makeText(this, "Welcome " + username.getText(), Toast.LENGTH_LONG)
				.show();
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
